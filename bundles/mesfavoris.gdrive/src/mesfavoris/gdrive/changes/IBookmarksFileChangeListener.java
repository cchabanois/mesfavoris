package mesfavoris.gdrive.changes;

import com.google.api.services.drive.model.Change;

import mesfavoris.model.BookmarkId;

public interface IBookmarksFileChangeListener {

	 public void bookmarksFileChanged(BookmarkId bookmarkFolderId, Change change);
	
}
