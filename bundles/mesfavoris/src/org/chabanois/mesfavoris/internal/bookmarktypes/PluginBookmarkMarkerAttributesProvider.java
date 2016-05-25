package org.chabanois.mesfavoris.internal.bookmarktypes;

import java.util.List;

import org.chabanois.mesfavoris.bookmarktype.BookmarkMarkerDescriptor;
import org.chabanois.mesfavoris.bookmarktype.IBookmarkMarkerAttributesProvider;
import org.chabanois.mesfavoris.model.Bookmark;

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
	public BookmarkMarkerDescriptor getMarkerDescriptor(Bookmark bookmark) {
		return getBookmarkMarkerAttributesProvider().getMarkerDescriptor(bookmark);
	}

	
	
}
