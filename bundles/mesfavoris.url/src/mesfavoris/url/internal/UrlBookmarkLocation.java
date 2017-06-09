package mesfavoris.url.internal;

import java.net.URL;

import mesfavoris.bookmarktype.IBookmarkLocation;

public class UrlBookmarkLocation implements IBookmarkLocation {
	private final URL url;

	public UrlBookmarkLocation(URL url) {
		this.url = url;
	}

	public URL getUrl() {
		return url;
	}

}
