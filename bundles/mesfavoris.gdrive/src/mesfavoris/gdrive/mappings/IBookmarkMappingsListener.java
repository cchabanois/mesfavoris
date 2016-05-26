package mesfavoris.gdrive.mappings;

import mesfavoris.model.BookmarkId;

public interface IBookmarkMappingsListener {

	void mappingAdded(BookmarkId bookmarkFolderId);
	
	void mappingRemoved(BookmarkId bookmarkFolderId);
}
