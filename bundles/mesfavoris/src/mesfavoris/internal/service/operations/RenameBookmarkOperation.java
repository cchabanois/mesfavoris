package mesfavoris.internal.service.operations;

import mesfavoris.BookmarksException;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;

public class RenameBookmarkOperation {
	private final BookmarkDatabase bookmarkDatabase;

	public RenameBookmarkOperation(BookmarkDatabase bookmarkDatabase) {
		this.bookmarkDatabase = bookmarkDatabase;
	}

	public void renameBookmark(BookmarkId bookmarkId, String newName) throws BookmarksException {
		bookmarkDatabase.modify(bookmarksTreeModifier -> {
			bookmarksTreeModifier.setPropertyValue(bookmarkId, Bookmark.PROPERTY_NAME, newName);
		});
	}

}
