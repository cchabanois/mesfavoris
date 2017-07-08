package mesfavoris.internal.shortcuts;

import mesfavoris.bookmarktype.IBookmarkLocation;
import mesfavoris.model.BookmarkId;

public class ShortcutBookmarkLocation implements IBookmarkLocation {
	private final BookmarkId bookmarkId;
	
	public ShortcutBookmarkLocation(BookmarkId bookmarkId) {
		this.bookmarkId = bookmarkId;
	}
	
	public BookmarkId getBookmarkId() {
		return bookmarkId;
	}
}
