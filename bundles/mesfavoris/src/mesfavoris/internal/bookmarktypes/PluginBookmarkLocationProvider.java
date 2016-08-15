package mesfavoris.internal.bookmarktypes;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import mesfavoris.bookmarktype.IBookmarkLocation;
import mesfavoris.bookmarktype.IBookmarkLocationProvider;
import mesfavoris.model.Bookmark;

public class PluginBookmarkLocationProvider implements IBookmarkLocationProvider {
	private final BookmarkTypeConfigElementLoader loader = new BookmarkTypeConfigElementLoader();
	private BookmarkLocationProvider bookmarkPropertiesProvider;

	private synchronized BookmarkLocationProvider getBookmarkPropertiesProvider() {
		if (bookmarkPropertiesProvider != null) {
			return bookmarkPropertiesProvider;
		}
		List<IBookmarkLocationProvider> providers = loader.load("locationProvider");
		this.bookmarkPropertiesProvider = new BookmarkLocationProvider(providers);
		return bookmarkPropertiesProvider;
	}

	@Override
	public IBookmarkLocation getBookmarkLocation(Bookmark bookmark, IProgressMonitor monitor) {
		return getBookmarkPropertiesProvider().getBookmarkLocation(bookmark, monitor);
	}
}
