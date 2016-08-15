package mesfavoris.url;

import static mesfavoris.url.UrlBookmarkProperties.PROP_URL;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;

import mesfavoris.bookmarktype.IBookmarkLocation;
import mesfavoris.bookmarktype.IBookmarkLocationProvider;
import mesfavoris.model.Bookmark;

public class UrlBookmarkLocationProvider implements IBookmarkLocationProvider {

	@Override
	public IBookmarkLocation getBookmarkLocation(Bookmark bookmark, IProgressMonitor monitor) {
		String url = bookmark.getPropertyValue(PROP_URL);
		if (url == null) {
			return null;
		}
		try {
			return new UrlBookmarkLocation(new URL(url));
		} catch (MalformedURLException e) {
			return null;
		}
	}

}
