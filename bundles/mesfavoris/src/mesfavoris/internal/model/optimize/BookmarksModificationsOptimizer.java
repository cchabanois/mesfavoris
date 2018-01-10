package mesfavoris.internal.model.optimize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mesfavoris.internal.model.compare.BookmarkComparer;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.model.modification.BookmarkDeletedModification;
import mesfavoris.model.modification.BookmarkPropertiesModification;
import mesfavoris.model.modification.BookmarksAddedModification;
import mesfavoris.model.modification.BookmarksModification;
import mesfavoris.model.modification.BookmarksMovedModification;

/**
 * Optimize bookmarks modifications (removing / merging when possible)
 * 
 * @author cchabanois
 *
 */
public class BookmarksModificationsOptimizer {
	private final BookmarkComparer bookmarkComparer = new BookmarkComparer();
	
	public List<BookmarksModification> optimize(List<BookmarksModification> modifications) {
		if (modifications.isEmpty()) {
			return new ArrayList<BookmarksModification>(modifications);
		}
		BookmarksTree sourceTree = modifications.get(0).getSourceTree();
		BookmarksTree targetTree = modifications.get(modifications.size() - 1).getTargetTree();
		// for now we only check if tree has been modified
		if (isSameTree(sourceTree, targetTree, modifications)) {
			return Collections.emptyList();
		} else {
			return new ArrayList<BookmarksModification>(modifications);
		}
	}

	private boolean isSameTree(BookmarksTree sourceTree, BookmarksTree targetTree,
			List<BookmarksModification> modifications) {
		for (BookmarksModification modification : modifications) {
			if (modification instanceof BookmarkDeletedModification) {
				if (!modificationCancelled(sourceTree, targetTree, (BookmarkDeletedModification) modification)) {
					return false;
				}
			} else if (modification instanceof BookmarksAddedModification) {
				if (!modificationCancelled(sourceTree, targetTree, (BookmarksAddedModification) modification)) {
					return false;
				}
			} else if (modification instanceof BookmarkPropertiesModification) {
				if (!modificationCancelled(sourceTree, targetTree, (BookmarkPropertiesModification) modification)) {
					return false;
				}
			} else if (modification instanceof BookmarksMovedModification) {
				if (!modificationCancelled(sourceTree, targetTree, (BookmarksMovedModification) modification)) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean modificationCancelled(BookmarksTree sourceTree, BookmarksTree targetTree,
			BookmarkDeletedModification modification) {
		return !isBookmarkModifiedOrDeleted(sourceTree, targetTree, modification.getBookmarkId());
	}

	private boolean modificationCancelled(BookmarksTree sourceTree, BookmarksTree targetTree,
			BookmarksAddedModification modification) {
		return !modification.getBookmarks().stream().map(bookmark -> bookmark.getId())
				.filter(bookmarkId -> targetTree.getBookmark(bookmarkId) != null).findAny().isPresent();
	}

	private boolean modificationCancelled(BookmarksTree sourceTree, BookmarksTree targetTree,
			BookmarkPropertiesModification modification) {
		return !isBookmarkModifiedOrDeleted(sourceTree, targetTree, modification.getBookmarkId());
	}

	private boolean modificationCancelled(BookmarksTree sourceTree, BookmarksTree targetTree,
			BookmarksMovedModification modification) {
		for (BookmarkId bookmarkId : modification.getBookmarkIds()) {
			if (!isAtSamePlace(sourceTree, targetTree, bookmarkId)) {
				return false;
			}
		}
		return true;
	}

	private boolean isAtSamePlace(BookmarksTree sourceTree, BookmarksTree targetTree, BookmarkId bookmarkId) {
		Bookmark sourceBookmark = sourceTree.getBookmark(bookmarkId);
		Bookmark targetBookmark = targetTree.getBookmark(bookmarkId);
		if (targetBookmark == null) {
			return sourceBookmark == null;
		}
		if (sourceBookmark == null) {
			return targetBookmark == null;
		}
		Bookmark sourceParentBookmark = sourceTree.getParentBookmark(bookmarkId);
		Bookmark targetParentBookmark = targetTree.getParentBookmark(bookmarkId);
		if (targetParentBookmark == null) {
			return sourceParentBookmark == null;
		}
		if (sourceParentBookmark == null) {
			return targetParentBookmark == null;
		}
		List<Bookmark> sourceChildren = sourceTree.getChildren(sourceParentBookmark.getId());
		List<Bookmark> targetChildren = sourceTree.getChildren(sourceParentBookmark.getId());
		return sourceChildren.indexOf(sourceBookmark) == targetChildren.indexOf(targetBookmark);
	}

	private boolean isBookmarkModifiedOrDeleted(BookmarksTree sourceTree, BookmarksTree targetTree,
			BookmarkId bookmarkId) {
		Bookmark sourceBookmark = sourceTree.getBookmark(bookmarkId);
		Bookmark targetBookmark = targetTree.getBookmark(bookmarkId);
		if (targetBookmark == null) {
			return sourceBookmark == null;
		}
		if (sourceBookmark == null) {
			return targetBookmark == null;
		}
		return !bookmarkComparer.compare(sourceBookmark, targetBookmark).isEmpty();
	}

}
