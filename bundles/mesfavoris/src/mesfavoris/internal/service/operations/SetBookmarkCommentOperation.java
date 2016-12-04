package mesfavoris.internal.service.operations;

import mesfavoris.BookmarksException;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;

public class SetBookmarkCommentOperation {
	private final BookmarkDatabase bookmarkDatabase;

	public SetBookmarkCommentOperation(BookmarkDatabase bookmarkDatabase) {
		this.bookmarkDatabase = bookmarkDatabase;
	}

	public void setComment(final BookmarkId bookmarkId, final String comment) throws BookmarksException {
		bookmarkDatabase.modify(bookmarksTreeModifier -> {
			if (comment.length() == 0) {
				bookmarksTreeModifier.setPropertyValue(bookmarkId, Bookmark.PROPERTY_COMMENT, null);
			} else {
				bookmarksTreeModifier.setPropertyValue(bookmarkId, Bookmark.PROPERTY_COMMENT, comment);
			}
		});
	}

}
