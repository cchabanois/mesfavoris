package mesfavoris.model.modification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mesfavoris.internal.model.optimize.BookmarksModificationsOptimizer;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;

public class BookmarksTreeModifier implements IBookmarksTreeModifier {
	private final BookmarksTree originalTree;
	private BookmarksTree currentTree;
	private final List<BookmarksModification> modifications = new ArrayList<BookmarksModification>();
	private final BookmarksModificationsOptimizer bookmarksModificationsOptimizer = new BookmarksModificationsOptimizer();
	
	public BookmarksTreeModifier(BookmarksTree bookmarksTree) {
		this.originalTree = bookmarksTree;
		this.currentTree = bookmarksTree;
	}

	@Override
	public void addBookmarks(BookmarkId parentId, List<Bookmark> bookmarks) {
		BookmarksTree sourceTree = currentTree;
		currentTree = currentTree.addBookmarks(parentId, bookmarks);
		if (currentTree != sourceTree) {
			modifications.add(new BookmarksAddedModification(sourceTree, currentTree, parentId, bookmarks));
		}
	}

	@Override
	public void addBookmarksAfter(BookmarkId parentId, BookmarkId existingBookmarkId, List<Bookmark> bookmarks) {
		BookmarksTree sourceTree = currentTree;
		currentTree = currentTree.addBookmarksAfter(parentId, existingBookmarkId, bookmarks);
		if (currentTree != sourceTree) {
			modifications.add(new BookmarksAddedModification(sourceTree, currentTree, parentId, bookmarks));
		}
	}

	@Override
	public void addBookmarksBefore(BookmarkId parentId, BookmarkId existingBookmarkId, List<Bookmark> bookmarks) {
		BookmarksTree sourceTree = currentTree;
		currentTree = currentTree.addBookmarksBefore(parentId, existingBookmarkId, bookmarks);
		if (currentTree != sourceTree) {
			modifications.add(new BookmarksAddedModification(sourceTree, currentTree, parentId, bookmarks));
		}
	}

	@Override
	public void move(List<BookmarkId> bookmarkIds, BookmarkId newParentId) {
		BookmarksTree sourceTree = currentTree;
		currentTree = currentTree.move(bookmarkIds, newParentId);
		if (currentTree != sourceTree) {
			modifications.add(new BookmarksMovedModification(sourceTree, currentTree, newParentId, bookmarkIds));
		}
	}

	@Override
	public void moveAfter(List<BookmarkId> bookmarkIds, BookmarkId newParentId, BookmarkId existingBookmarkId) {
		BookmarksTree sourceTree = currentTree;
		currentTree = currentTree.moveAfter(bookmarkIds, newParentId, existingBookmarkId);
		if (currentTree != sourceTree) {
			modifications.add(new BookmarksMovedModification(sourceTree, currentTree, newParentId, bookmarkIds));
		}
	}

	@Override
	public void moveBefore(List<BookmarkId> bookmarkIds, BookmarkId newParentId, BookmarkId existingBookmarkId) {
		BookmarksTree sourceTree = currentTree;
		currentTree = currentTree.moveBefore(bookmarkIds, newParentId, existingBookmarkId);
		if (currentTree != sourceTree) {
			modifications.add(new BookmarksMovedModification(sourceTree, currentTree, newParentId, bookmarkIds));
		}
	}

	@Override
	public void deleteBookmark(BookmarkId bookmarkId, boolean recurse) {
		BookmarksTree sourceTree = currentTree;
		if (recurse && !(currentTree.getBookmark(bookmarkId) instanceof BookmarkFolder)) {
			recurse = false;
		}
		currentTree = currentTree.deleteBookmark(bookmarkId, recurse);
		if (currentTree != sourceTree) {
			BookmarkFolder bookmarkFolder = sourceTree.getParentBookmark(bookmarkId);
			modifications.add(new BookmarkDeletedModification(sourceTree, currentTree, bookmarkFolder.getId(),
					bookmarkId, recurse));
		}
	}

	public BookmarksTree getCurrentTree() {
		return currentTree;
	}

	public BookmarksTree getOriginalTree() {
		return originalTree;
	}

	public List<BookmarksModification> getModifications() {
		return new ArrayList<BookmarksModification>(modifications);
	}

	@Override
	public void setPropertyValue(BookmarkId bookmarkId, String propertyName, String propertyValue) {
		BookmarksTree sourceTree = currentTree;
		currentTree = currentTree.setPropertyValue(bookmarkId, propertyName, propertyValue);
		if (currentTree != sourceTree) {
			modifications.add(new BookmarkPropertiesModification(sourceTree, currentTree, bookmarkId));
		}
	}

	@Override
	public void setProperties(BookmarkId bookmarkId, Map<String, String> properties) {
		BookmarksTree sourceTree = currentTree;
		currentTree = currentTree.setProperties(bookmarkId, properties);
		if (currentTree != sourceTree) {
			modifications.add(new BookmarkPropertiesModification(sourceTree, currentTree, bookmarkId));
		}
	}
	
	public void optimize() {
		List<BookmarksModification> newModifications  = bookmarksModificationsOptimizer.optimize(modifications);
		this.modifications.clear();
		this.modifications.addAll(newModifications);
		if (modifications.size() > 0) {
			currentTree = modifications.get(modifications.size()-1).getTargetTree();
		} else {
			currentTree = originalTree;
		}
	}
	
}
