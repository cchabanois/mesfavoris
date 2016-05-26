package mesfavoris.bookmarktype;

import mesfavoris.model.Bookmark;

public interface IBookmarkMarkerAttributesProvider {
	
	public BookmarkMarkerDescriptor getMarkerDescriptor(Bookmark bookmark);
	
}
