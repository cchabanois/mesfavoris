package mesfavoris.url.internal;

import static mesfavoris.bookmarktype.BookmarkPropertiesProviderUtil.getFirstElement;
import static mesfavoris.model.Bookmark.PROPERTY_NAME;
import static mesfavoris.url.UrlBookmarkProperties.PROP_ICON;
import static mesfavoris.url.UrlBookmarkProperties.PROP_URL;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import mesfavoris.bookmarktype.AbstractBookmarkPropertiesProvider;

public class UrlBookmarkPropertiesProvider extends AbstractBookmarkPropertiesProvider {

	private static final int TARGET_ICON_SIZE = 32;
	private static final String PNG_FORMAT_NAME = "png";
	private static final int MAX_BODY_SIZE = 32768;

	public UrlBookmarkPropertiesProvider() {

	}

	@Override
	public void addBookmarkProperties(Map<String, String> bookmarkProperties, IWorkbenchPart part, ISelection selection,
			IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
		Object selected = getFirstElement(selection);
		if (!(selected instanceof URL)) {
			return;
		}
		URL url = (URL) selected;
		putIfAbsent(bookmarkProperties, PROP_URL, url.toString());

		if (!isPresent(bookmarkProperties, PROPERTY_NAME) || !isPresent(bookmarkProperties, PROP_ICON)) {
			Optional<Document> document = parse(url, subMonitor.newChild(50));
			if (document.isPresent()) {
				getTitle(document.get()).ifPresent(title -> putIfAbsent(bookmarkProperties, PROPERTY_NAME, title));
				getFavIconAsBase64(url, document.get(), subMonitor.newChild(50))
						.ifPresent(favIcon -> putIfAbsent(bookmarkProperties, PROP_ICON, favIcon));
			} else {
				getFavIconUrl(url).flatMap(u -> getFavIconAsBase64(u, subMonitor.newChild(50)))
						.ifPresent(favIcon -> putIfAbsent(bookmarkProperties, PROP_ICON, favIcon));
			}
		}
		putIfAbsent(bookmarkProperties, PROPERTY_NAME, url.toString());
	}

	private Optional<Document> parse(URL url, IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Getting html document", 100);
		try {
			Response response = Jsoup.connect(url.toString()).followRedirects(false).timeout(2000)
					.maxBodySize(MAX_BODY_SIZE).execute();
			if (response.statusCode() != 200) {
				return Optional.empty();
			}
			return Optional.of(response.parse());
		} catch (IOException e) {
			return Optional.empty();
		}
	}

	private Optional<String> getFavIconAsBase64(URL url, Document document, IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Getting favIcon", 100);
		Optional<String> favIconUrl = getFavIconUrl(document);
		if (!favIconUrl.isPresent()) {
			favIconUrl = getFavIconUrl(url);
		}
		return favIconUrl.flatMap(u -> getFavIconAsBase64(u, subMonitor));
	}

	private Optional<String> getFavIconUrl(URL url) {
		try {
			return Optional.of(new URL(url.getProtocol(), url.getHost(), url.getPort(), "/favicon.ico").toString());
		} catch (MalformedURLException e) {
			return Optional.empty();
		}
	}

	private Optional<String> getFavIconAsBase64(String favIconUrl, IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Getting favIcon", 100);
		Response resultImageResponse;
		try {
			resultImageResponse = Jsoup.connect(favIconUrl).ignoreContentType(true).execute();
		} catch (IOException e) {
			return Optional.empty();
		}
		byte[] bytes = resultImageResponse.bodyAsBytes();
		Optional<BufferedImage> bufferedImage = getBufferedImage(bytes, TARGET_ICON_SIZE);
		return bufferedImage.map(imgData -> Base64.getEncoder().encodeToString(asBytes(imgData, PNG_FORMAT_NAME)));
	}

	private byte[] asBytes(BufferedImage imageData, String formatName) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			ImageIO.write(imageData, formatName, baos);
			return baos.toByteArray();
		} catch (IOException e) {
			return new byte[0];
		}
	}

	private List<BufferedImage> getBufferedImages(byte[] favIconBytes) throws IOException {
		try (ImageInputStream stream = ImageIO.createImageInputStream(new ByteArrayInputStream(favIconBytes))) {
			Iterator<ImageReader> iter = ImageIO.getImageReaders(stream);
			if (!iter.hasNext()) {
				return Collections.emptyList();
			}
			ImageReader reader = (ImageReader) iter.next();
			try {
				ImageReadParam param = reader.getDefaultReadParam();
				reader.setInput(stream, true, true);
				List<BufferedImage> bufferedImages = new ArrayList<>();
				int i = 0;
				while (true) {
					try {
						BufferedImage bufferedImage = reader.read(i, param);
						bufferedImages.add(bufferedImage);
					} catch (IndexOutOfBoundsException e) {
						return bufferedImages;
					}
					i++;
				}
			} finally {
				reader.dispose();
			}
		}
	}

	private Optional<BufferedImage> getBufferedImage(byte[] favIconBytes, int targetImageSize) {
		List<BufferedImage> bufferedImages;
		try {
			bufferedImages = getBufferedImages(favIconBytes);
		} catch (IOException e) {
			return Optional.empty();
		}
		Optional<BufferedImage> optionalBufferedImage = bufferedImages.stream()
				.sorted((image1, image2) -> distanceFromTargetImageSize(image1, targetImageSize)
						- distanceFromTargetImageSize(image2, targetImageSize))
				.findFirst();
		if (!optionalBufferedImage.isPresent()) {
			return Optional.empty();
		}
		BufferedImage bufferedImage = optionalBufferedImage.get();
		if (bufferedImage.getWidth() <= targetImageSize && bufferedImage.getHeight() <= targetImageSize) {
			return Optional.of(bufferedImage);
		}
		return Optional.of(resizeImage(bufferedImage, TARGET_ICON_SIZE, TARGET_ICON_SIZE));
	}

	private int distanceFromTargetImageSize(BufferedImage image, int targetImageSize) {
		return Math.abs(image.getWidth() * image.getHeight() - targetImageSize * targetImageSize);
	}

	private static BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
		return Scalr.resize(originalImage, Method.ULTRA_QUALITY, width, height);
	}

	private Optional<String> getFavIconUrl(Document document) {
		Element head = document.head();
		if (head == null) {
			return Optional.empty();
		}
		Element link = head.select("link[href~=.*\\.(ico|png|gif)]").first();
		if (link == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(link.attr("abs:href"));
	}

	private Optional<String> getTitle(Document document) {
		return Optional.ofNullable(document.title());
	}

}
