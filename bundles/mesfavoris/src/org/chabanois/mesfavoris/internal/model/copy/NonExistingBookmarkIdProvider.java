package org.chabanois.mesfavoris.internal.model.copy;

import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.model.BookmarksTree;

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