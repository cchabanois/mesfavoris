package org.chabanois.mesfavoris.model.modification;

import java.util.List;

import org.chabanois.mesfavoris.model.Bookmark;
import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.model.BookmarksTree;

import com.google.common.collect.Lists;

public class BookmarksAddedModification extends BookmarksModification {
	private final BookmarkId parentId;
	private final List<Bookmark> bookmarks;

	public BookmarksAddedModification(BookmarksTree sourceTree, BookmarksTree targetTree, BookmarkId parentId,
			List<Bookmark> bookmarks) {
		super(sourceTree, targetTree);
		this.parentId = parentId;
		this.bookmarks = Lists.newArrayList(bookmarks);
	}

	public List<Bookmark> getBookmarks() {
		return bookmarks;
	}

	public BookmarkId getParentId() {
		return parentId;
	}

	@Override
	public String toString() {
		return "BookmarksAddedModification [parentId=" + parentId + ", bookmarks=" + bookmarks + "]";
	}

}