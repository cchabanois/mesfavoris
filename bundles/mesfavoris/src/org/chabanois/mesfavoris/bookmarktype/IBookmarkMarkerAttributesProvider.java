package org.chabanois.mesfavoris.bookmarktype;

import org.chabanois.mesfavoris.model.Bookmark;

public interface IBookmarkMarkerAttributesProvider {
	
	public BookmarkMarkerDescriptor getMarkerDescriptor(Bookmark bookmark);
	
}
