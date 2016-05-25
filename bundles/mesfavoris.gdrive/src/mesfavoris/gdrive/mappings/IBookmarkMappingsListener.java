package mesfavoris.gdrive.mappings;

import org.chabanois.mesfavoris.model.BookmarkId;

public interface IBookmarkMappingsListener {

	void mappingAdded(BookmarkId bookmarkFolderId);
	
	void mappingRemoved(BookmarkId bookmarkFolderId);
}
