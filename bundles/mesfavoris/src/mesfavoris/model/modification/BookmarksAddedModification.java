package mesfavoris.model.modification;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkFolder;
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

	public BookmarkId getAfterBookmarkId() {
		Bookmark bookmark = bookmarks.get(0);
		BookmarkFolder parentFolder = targetTree.getParentBookmark(bookmark.getId());
		if (parentFolder == null) {
			return null;
		}
		List<Bookmark> children = targetTree.getChildren(parentFolder.getId());
		int index = children.indexOf(bookmark);
		if (index == 0 || index == -1) {
			return null;
		}
		return children.get(index-1).getId();
	}
	
	public BookmarkId getBeforeBookmarkId() {
		Bookmark bookmark = bookmarks.get(bookmarks.size()-1);
		BookmarkFolder parentFolder = targetTree.getParentBookmark(bookmark.getId());
		if (parentFolder == null) {
			return null;
		}
		List<Bookmark> children = targetTree.getChildren(parentFolder.getId());
		int index = children.indexOf(bookmark);
		if (index == children.size()-1 || index == -1) {
			return null;
		}
		return children.get(index+1).getId();
	}	
	
	@Override
	public String toString() {
		return "BookmarksAddedModification [parentId=" + parentId + ", bookmarks=" + bookmarks + "]";
	}

}