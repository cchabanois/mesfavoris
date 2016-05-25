package org.chabanois.mesfavoris.internal.model.compare;

import java.util.List;
import java.util.Optional;

import org.chabanois.mesfavoris.internal.model.merge.BookmarksTreeMerger;
import org.chabanois.mesfavoris.model.Bookmark;
import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.model.BookmarksTree;
import org.chabanois.mesfavoris.model.modification.BookmarkPropertiesModification;
import org.chabanois.mesfavoris.model.modification.BookmarksModification;
import org.chabanois.mesfavoris.model.modification.BookmarksTreeModifier;

/**
 * Compare two bookmark trees
 * 
 * @author cchabanois
 *
 */
public class BookmarksTreeComparer {
	private final BookmarksTree sourceTree;
	private final BookmarksTree targetTree;

	public BookmarksTreeComparer(BookmarksTree sourceTree, BookmarksTree targetTree) {
		this.sourceTree = sourceTree;
		this.targetTree = targetTree;
	}

	public List<BookmarksModification> compareBookmarkFolder(BookmarkId bookmarkFolderId) {
		Bookmark sourceBookmark = sourceTree.getBookmark(bookmarkFolderId);
		Bookmark targetBookmark = targetTree.getBookmark(bookmarkFolderId);
		if (sourceBookmark == null) {
			throw new IllegalArgumentException("No source bookmark with id " + bookmarkFolderId);
		}
		if (targetBookmark == null) {
			throw new IllegalArgumentException("No target bookmark with id " + bookmarkFolderId);
		}
		BookmarksTreeMerger bookmarksTreeMerger = new BookmarksTreeMerger(sourceTree.subTree(bookmarkFolderId));
		BookmarksTreeModifier bookmarksTreeModifier = new BookmarksTreeModifier(targetTree);
		bookmarksTreeMerger.merge(bookmarksTreeModifier);
		return bookmarksTreeModifier.getModifications();
	}

	public Optional<BookmarkPropertiesModification> compareBookmarkProperties(BookmarkId bookmarkId) {
		Bookmark sourceBookmark = sourceTree.getBookmark(bookmarkId);
		Bookmark targetBookmark = targetTree.getBookmark(bookmarkId);
		if (sourceBookmark == null) {
			throw new IllegalArgumentException("No source bookmark with id " + bookmarkId);
		}
		if (targetBookmark == null) {
			throw new IllegalArgumentException("No target bookmark with id " + bookmarkId);
		}
		BookmarksTree currentTree = targetTree;
		currentTree = currentTree.setProperties(bookmarkId, sourceBookmark.getProperties());
		if (currentTree != targetTree) {
			return Optional.of(new BookmarkPropertiesModification(targetTree, currentTree, bookmarkId));
		} else {
			return Optional.empty();
		}
	}

}
