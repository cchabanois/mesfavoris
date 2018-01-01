package mesfavoris.model.modification;

import java.util.List;

import com.google.common.collect.Lists;

import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;

public class BookmarksMovedModification extends BookmarksModification {
	private final List<BookmarkId> bookmarkIds;
	private final BookmarkId newParentId;
	
	public BookmarksMovedModification(BookmarksTree sourceTree, BookmarksTree targetTree, BookmarkId newParentId, List<BookmarkId> bookmarkIds) {
		super(sourceTree, targetTree);
		this.newParentId = newParentId;
		this.bookmarkIds = Lists.newArrayList(bookmarkIds);
	}

	public List<BookmarkId> getBookmarkIds() {
		return bookmarkIds;
	}
	
	public BookmarkId getNewParentId() {
		return newParentId;
	}

	public BookmarkId getAfterBookmarkId() {
		BookmarkId bookmarkId = bookmarkIds.get(0);
		Bookmark bookmark = targetTree.getBookmark(bookmarkId);
		BookmarkFolder parentFolder = targetTree.getParentBookmark(bookmarkId);
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
		BookmarkId bookmarkId = bookmarkIds.get(bookmarkIds.size()-1);
		Bookmark bookmark = targetTree.getBookmark(bookmarkId);
		BookmarkFolder parentFolder = targetTree.getParentBookmark(bookmarkId);
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
		return "BookmarksMovedModification [bookmarkIds=" + bookmarkIds + ", newParentId=" + newParentId + "]";
	}
	
}
