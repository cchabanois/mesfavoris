package mesfavoris.internal.model.replay;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import mesfavoris.internal.model.compare.BookmarksTreeComparer;
import mesfavoris.internal.model.utils.BookmarksTreeUtils;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.model.modification.BookmarkDeletedModification;
import mesfavoris.model.modification.BookmarkPropertiesModification;
import mesfavoris.model.modification.BookmarksAddedModification;
import mesfavoris.model.modification.BookmarksModification;
import mesfavoris.model.modification.BookmarksMovedModification;
import mesfavoris.model.modification.BookmarksTreeModifier;

/**
 * Replay a list of modifications
 * 
 * @author cchabanois
 *
 */
public class ModificationsReplayer {
	private final List<BookmarksModification> modifications;

	public ModificationsReplayer(List<BookmarksModification> modifications) {
		this.modifications = modifications;
	}

	/**
	 * Replay a list of modifications
	 * 
	 * @param bookmarksTreeModifier
	 * @return the modifications that could not be replayed
	 */
	public List<BookmarksModification> replayModifications(BookmarksTreeModifier bookmarksTreeModifier) {
		return modifications.stream().filter(m -> !replayModification(bookmarksTreeModifier, m))
				.collect(Collectors.toList());
	}

	private boolean replayModification(BookmarksTreeModifier bookmarksTreeModifier,
			BookmarksModification modification) {
		if (modification instanceof BookmarkDeletedModification) {
			return replayDeleteModification(bookmarksTreeModifier, (BookmarkDeletedModification) modification);
		} else if (modification instanceof BookmarkPropertiesModification) {
			return replayBookmarkPropertiesModification(bookmarksTreeModifier,
					(BookmarkPropertiesModification) modification);
		} else if (modification instanceof BookmarksAddedModification) {
			return replayBookmarksAddedModification(bookmarksTreeModifier, (BookmarksAddedModification) modification);
		} else if (modification instanceof BookmarksMovedModification) {
			return replayBookmarksMovedModification(bookmarksTreeModifier, (BookmarksMovedModification) modification);
		} else {
			// unknown bookmark modification
			return false;
		}
	}

	private boolean replayBookmarksMovedModification(BookmarksTreeModifier bookmarksTreeModifier,
			BookmarksMovedModification modification) {
		try {
			Bookmark sourceBookmarkBefore = BookmarksTreeUtils.getBookmarkBefore(modification.getTargetTree(),
					modification.getBookmarkIds().get(0),
					b -> bookmarksTreeModifier.getCurrentTree().getBookmark(b.getId()) != null);
			bookmarksTreeModifier.moveAfter(modification.getBookmarkIds(), modification.getNewParentId(),
					sourceBookmarkBefore == null ? null : sourceBookmarkBefore.getId());
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	private boolean replayBookmarksAddedModification(BookmarksTreeModifier bookmarksTreeModifier,
			BookmarksAddedModification modification) {
		try {
			Bookmark sourceBookmarkBefore = BookmarksTreeUtils.getBookmarkBefore(modification.getTargetTree(),
					modification.getBookmarks().get(0).getId(),
					b -> bookmarksTreeModifier.getCurrentTree().getBookmark(b.getId()) != null);
			bookmarksTreeModifier.addBookmarksAfter(modification.getParentId(),
					sourceBookmarkBefore == null ? null : sourceBookmarkBefore.getId(), modification.getBookmarks());
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	private boolean replayBookmarkPropertiesModification(BookmarksTreeModifier bookmarksTreeModifier,
			BookmarkPropertiesModification modification) {
		try {
			if (bookmarkExists(modifications.get(0).getSourceTree(), modification.getBookmarkId())
					&& bookmarkExists(bookmarksTreeModifier.getOriginalTree(), modification.getBookmarkId())
					&& hasPropertiesChanged(bookmarksTreeModifier.getOriginalTree(),
							modifications.get(0).getSourceTree(), modification.getBookmarkId())) {
				return false;
			}
			Map<String, String> newProperties = modification.getTargetTree().getBookmark(modification.getBookmarkId())
					.getProperties();
			bookmarksTreeModifier.setProperties(modification.getBookmarkId(), newProperties);
			return true;
		} catch (IllegalStateException | IllegalArgumentException e) {
			return false;
		}
	}

	private boolean bookmarkExists(BookmarksTree bookmarksTree, BookmarkId bookmarkId) {
		return bookmarksTree.getBookmark(bookmarkId) != null;
	}

	private boolean hasPropertiesChanged(BookmarksTree sourceTree, BookmarksTree targetTree, BookmarkId bookmarkId) {
		BookmarksTreeComparer comparer = new BookmarksTreeComparer(sourceTree, targetTree);
		return comparer.compareBookmarkProperties(bookmarkId).isPresent();
	}

	private boolean replayDeleteModification(BookmarksTreeModifier bookmarksTreeModifier,
			BookmarkDeletedModification modification) {
		try {
			if (modification.isRecursive()
					&& bookmarkExists(modifications.get(0).getSourceTree(), modification.getBookmarkId())
					&& bookmarkExists(bookmarksTreeModifier.getOriginalTree(), modification.getBookmarkId())
					&& hasNewOrModifiedBookmark(bookmarksTreeModifier.getOriginalTree(),
							modifications.get(0).getSourceTree(), modification.getBookmarkId())) {
				return false;
			}
			bookmarksTreeModifier.deleteBookmark(modification.getBookmarkId(), modification.isRecursive());
			return true;
		} catch (IllegalStateException e) {
			return false;
		}
	}

	private boolean hasNewOrModifiedBookmark(BookmarksTree sourceTree, BookmarksTree targetTree,
			BookmarkId bookmarkFolderId) {
		BookmarksTreeComparer comparer = new BookmarksTreeComparer(sourceTree, targetTree);
		List<BookmarksModification> modifications = comparer.compareBookmarkFolder(bookmarkFolderId);
		return modifications.stream()
				.filter(m -> m instanceof BookmarksAddedModification || m instanceof BookmarkPropertiesModification)
				.findAny().isPresent();
	}

}
