package mesfavoris.internal.service.operations;

import mesfavoris.internal.numberedbookmarks.BookmarkNumber;
import mesfavoris.internal.numberedbookmarks.NumberedBookmarks;
import mesfavoris.model.BookmarkId;

public class AddNumberedBookmarkOperation {
	private final NumberedBookmarks numberedBookmarks;
	
	public AddNumberedBookmarkOperation(NumberedBookmarks numberedBookmarks) {
		this.numberedBookmarks = numberedBookmarks;
	}
	
	public void addNumberedBookmark(BookmarkId bookmarkId, BookmarkNumber bookmarkNumber) {
		numberedBookmarks.set(bookmarkNumber, bookmarkId);
	}
	
}
