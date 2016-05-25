package org.chabanois.mesfavoris.internal.bookmarktypes;

import java.util.ArrayList;
import java.util.List;

import org.chabanois.mesfavoris.bookmarktype.BookmarkMarkerDescriptor;
import org.chabanois.mesfavoris.bookmarktype.IBookmarkMarkerAttributesProvider;
import org.chabanois.mesfavoris.model.Bookmark;

public class BookmarkMarkerAttributesProvider implements
		IBookmarkMarkerAttributesProvider {
	private final List<IBookmarkMarkerAttributesProvider> bookmarkMarkerAttributesProviders;

	public BookmarkMarkerAttributesProvider(
			List<IBookmarkMarkerAttributesProvider> bookmarkMarkerAttributesProviders) {
		this.bookmarkMarkerAttributesProviders = new ArrayList<IBookmarkMarkerAttributesProvider>(
				bookmarkMarkerAttributesProviders);
	}

	@Override
	public BookmarkMarkerDescriptor getMarkerDescriptor(Bookmark bookmark) {
		for (IBookmarkMarkerAttributesProvider provider : bookmarkMarkerAttributesProviders) {
			BookmarkMarkerDescriptor bookmarkMarkerDescriptor = provider
					.getMarkerDescriptor(bookmark);
			if (bookmarkMarkerDescriptor != null) {
				return bookmarkMarkerDescriptor;
			}
		}
		return null;
	}

}
