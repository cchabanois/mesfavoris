package mesfavoris.model.modification;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;

public class BookmarksAddedModification extends BookmarksModification {
	private final BookmarkId parentId;
	private final List<Bookmark> bookmarks;

	public BookmarksAddedModification(BookmarksTree sourceTree, BookmarksTree targetTree, BookmarkId parentId,
			List<Bookmark> bookmarks) {
		super(sourceTree, targetTree);
		this.parentId = parentId;
		this.bookmarks = Collections.unmodifiableList(Lists.newArrayList(bookmarks));
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