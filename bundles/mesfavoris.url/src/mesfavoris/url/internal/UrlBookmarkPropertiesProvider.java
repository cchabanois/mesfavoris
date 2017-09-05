package mesfavoris.url.internal;

import static mesfavoris.bookmarktype.BookmarkPropertiesProviderUtil.getFirstElement;
import static mesfavoris.url.UrlBookmarkProperties.PROP_FAVICON;
import static mesfavoris.url.UrlBookmarkProperties.PROP_URL;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.ui.IWorkbenchPart;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import mesfavoris.bookmarktype.AbstractBookmarkPropertiesProvider;
import mesfavoris.model.Bookmark;

public class UrlBookmarkPropertiesProvider extends AbstractBookmarkPropertiesProvider {

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

		Optional<Document> document = parse(url, subMonitor.newChild(50));
		if (document.isPresent()) {
			getTitle(document.get()).ifPresent(title -> putIfAbsent(bookmarkProperties, Bookmark.PROPERTY_NAME, title));
			getFavIconAsBase64(url, document.get(), subMonitor.newChild(50))
					.ifPresent(favIcon -> putIfAbsent(bookmarkProperties, PROP_FAVICON, favIcon));
		} else {
			getFavIconUrl(url).flatMap(u -> getFavIconAsBase64(u, subMonitor.newChild(50)))
					.ifPresent(favIcon -> putIfAbsent(bookmarkProperties, PROP_FAVICON, favIcon));
		}
		putIfAbsent(bookmarkProperties, Bookmark.PROPERTY_NAME, url.toString());
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
		Optional<ImageData> imageData = get16x16ImageData(bytes);
		// Issue with transparent color when using SWT.IMAGE_PNG
		return imageData.map(imgData->Base64.getEncoder().encodeToString(asBytes(imgData, SWT.IMAGE_ICO)));
	}

	private byte[] asBytes(ImageData imageData, int format) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			ImageLoader loader = new ImageLoader();
			loader.data = new ImageData[] { imageData };
			loader.save(baos, format);
			return baos.toByteArray();
		} catch (IOException e) {
			return new byte[0];
		}
	}
	
	private Optional<ImageData> get16x16ImageData(byte[] favIconBytes) {
		ImageData[] imageDatas;
		try {
			imageDatas = new ImageLoader().load(new ByteArrayInputStream(favIconBytes));
		} catch (SWTException e) {
			return Optional.empty();
		}
		Optional<ImageData> optionalImageData = Arrays.stream(imageDatas).sorted((imageData1,
				imageData2) -> distanceFrom16x16ImageData(imageData1) - distanceFrom16x16ImageData(imageData2))
				.findFirst();
		if (!optionalImageData.isPresent()) {
			return Optional.empty();
		}
		ImageData imageData = optionalImageData.get();
		if (imageData.width <= 16 && imageData.height <= 16) {
			return Optional.of(imageData);
		}
		return Optional.of(imageData.scaledTo(16, 16));
	}

	private int distanceFrom16x16ImageData(ImageData imageData) {
		return imageData.width * imageData.height - 16 * 16;
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
