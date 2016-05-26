package mesfavoris.internal.model.copy;

import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;

public class NonExistingBookmarkIdProvider implements IBookmarkCopyIdProvider {
	private final BookmarksTree bookmarksTree;

	public NonExistingBookmarkIdProvider(BookmarksTree bookmarksTree) {
		this.bookmarksTree = bookmarksTree;
	}

	@Override
	public BookmarkId getBookmarkCopyId(BookmarkId sourceBookmarkId) {
		if (bookmarksTree.getBookmark(sourceBookmarkId) == null) {
			return sourceBookmarkId;
		} else {
			return new BookmarkId();
		}
	}

}