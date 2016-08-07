package mesfavoris.internal.bookmarktypes;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import mesfavoris.bookmarktype.BookmarkMarkerDescriptor;
import mesfavoris.bookmarktype.IBookmarkMarkerAttributesProvider;
import mesfavoris.model.Bookmark;

public class PluginBookmarkMarkerAttributesProvider implements IBookmarkMarkerAttributesProvider {
	private final BookmarkTypeConfigElementLoader loader = new BookmarkTypeConfigElementLoader();
	private BookmarkMarkerAttributesProvider bookmarkMarkerAttributesProvider;
	
	private synchronized BookmarkMarkerAttributesProvider getBookmarkMarkerAttributesProvider() {
		if (bookmarkMarkerAttributesProvider != null) {
			return bookmarkMarkerAttributesProvider;
		}
		List<IBookmarkMarkerAttributesProvider> providers = loader.load("markerAttributesProvider");
		this.bookmarkMarkerAttributesProvider = new BookmarkMarkerAttributesProvider(providers);
		return bookmarkMarkerAttributesProvider;
	}
	
	@Override
	public BookmarkMarkerDescriptor getMarkerDescriptor(Bookmark bookmark, IProgressMonitor monitor) {
		return getBookmarkMarkerAttributesProvider().getMarkerDescriptor(bookmark, monitor);
	}

	
	
}
