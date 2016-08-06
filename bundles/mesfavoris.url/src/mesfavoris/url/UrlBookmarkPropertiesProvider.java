package mesfavoris.url;

import static mesfavoris.url.UrlBookmarkProperties.PROP_FAVICON;
import static mesfavoris.url.UrlBookmarkProperties.PROP_URL;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import mesfavoris.bookmarktype.AbstractBookmarkPropertiesProvider;
import mesfavoris.model.Bookmark;

public class UrlBookmarkPropertiesProvider extends AbstractBookmarkPropertiesProvider {

	public UrlBookmarkPropertiesProvider() {

	}

	@Override
	public void addBookmarkProperties(Map<String, String> bookmarkProperties, IWorkbenchPart part,
			ISelection selection, IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
		Object selected = getFirstElement(selection);
		if (!(selected instanceof URL)) {
			return;
		}
		URL url = (URL) selected;
		putIfAbsent(bookmarkProperties, PROP_URL, url.toString());

		parse(url, subMonitor.newChild(50)).ifPresent(document -> {
			getTitle(document).ifPresent(title -> putIfAbsent(bookmarkProperties, Bookmark.PROPERTY_NAME, title));
			getFavIconAsBase64(url, document, subMonitor.newChild(50))
					.ifPresent(favIcon -> putIfAbsent(bookmarkProperties, PROP_FAVICON, favIcon));
		});
		putIfAbsent(bookmarkProperties, Bookmark.PROPERTY_NAME, url.toString());
	}

	private Optional<Document> parse(URL url, IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Getting html document", 100);
		try {
			Response response = Jsoup.connect(url.toString()).followRedirects(false).timeout(2000).maxBodySize(8192).execute();
			if (response.statusCode() != 200) {
				return Optional.empty();
			}
			return Optional
					.of(response.parse());
		} catch (IOException e) {
			return Optional.empty();
		}
	}

	private Optional<String> getFavIconAsBase64(URL url, Document document, IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Getting favIcon", 100);
		String favIconUrl;
		try {
			favIconUrl = getFavIconUrl(document)
					.orElse(new URL(url.getProtocol(), url.getHost(), url.getPort(), "favicon.ico").toString());
		} catch (MalformedURLException e) {
			return Optional.empty();
		}
		return getFavIconAsBase64(favIconUrl);
	}

	private Optional<String> getFavIconAsBase64(String favIconUrl) {
		Response resultImageResponse;
		try {
			resultImageResponse = Jsoup.connect(favIconUrl).ignoreContentType(true).execute();
		} catch (IOException e) {
			return Optional.empty();
		}
		byte[] bytes = resultImageResponse.bodyAsBytes();
		Image image = null;
		try {
			image = new Image(Display.getCurrent(), new ByteArrayInputStream(bytes));
			return Optional.of(Base64.getEncoder().encodeToString(bytes));
		} catch (SWTException e) {
			return Optional.empty();
		} finally {
			if (image != null) {
				image.dispose();
			}
		}
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
