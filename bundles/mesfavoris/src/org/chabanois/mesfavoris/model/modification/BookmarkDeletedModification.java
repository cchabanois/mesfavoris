package org.chabanois.mesfavoris.model.modification;

import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.model.BookmarksTree;

public class BookmarkDeletedModification extends
		BookmarksModification {
	private final BookmarkId bookmarkParentId;
	private final BookmarkId bookmarkId;
	private final boolean recursive;
	
	public BookmarkDeletedModification(BookmarksTree sourceTree,
			BookmarksTree targetTree, BookmarkId bookmarkParentId, BookmarkId bookmarkId, boolean recursive) {
		super(sourceTree, targetTree);
		this.bookmarkParentId = bookmarkParentId;
		this.bookmarkId = bookmarkId;
		this.recursive = recursive;
	}

	public BookmarkId getBookmarkParentId() {
		return bookmarkParentId;
	}
	
	public BookmarkId getBookmarkId() {
		return bookmarkId;
	}

	public boolean isRecursive() {
		return recursive;
	}

	@Override
	public String toString() {
		return "BookmarkDeletedModification [bookmarkParentId=" + bookmarkParentId + ", bookmarkId=" + bookmarkId
				+ ", recursive=" + recursive + "]";
	}
	
	
	
}