package mesfavoris.internal.model.merge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.Lists;

import mesfavoris.internal.model.merge.BookmarksTreeIterable.Algorithm;
import mesfavoris.internal.model.merge.MinimumMovesSolver.Move;
import mesfavoris.internal.model.utils.BookmarksTreeUtils;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.model.modification.IBookmarksTreeModifier;

/**
 * Merge changes from a source {@link BookmarksTree} to target
 * {@link BookmarksTree} using a {@link IBookmarksTreeModifier}.
 * 
 * <p>
 * After merging, target {@link BookmarksTree} will be the same than source
 * {@link BookmarksTree}.
 * </p>
 * 
 * @author cchabanois
 *
 */
public class BookmarksTreeMerger {
	private final BookmarksTree sourceTree;

	public BookmarksTreeMerger(BookmarksTree sourceTree) {
		this.sourceTree = sourceTree;
	}

	public void merge(IBookmarksTreeModifier bookmarksTreeModifier) {
		// ordering is important
		handleBookmarksMovedToSameFolder(bookmarksTreeModifier);
		handleNewBookmarks(bookmarksTreeModifier);
		handleBookmarksMovedToOtherFolder(bookmarksTreeModifier);
		handleDeletedOrUpdatedBookmarks(bookmarksTreeModifier);
	}

	private void handleBookmarksMovedToSameFolder(IBookmarksTreeModifier bookmarksTreeModifier) {
		BookmarkId folderId = sourceTree.getRootFolder().getId();
		BookmarksTreeIterable postOrderTargetBookmarksTreeIterable = new BookmarksTreeIterable(
				bookmarksTreeModifier.getCurrentTree(), folderId, Algorithm.POST_ORDER,
				b -> b instanceof BookmarkFolder);
		for (Bookmark targetBookmark : postOrderTargetBookmarksTreeIterable) {
			BookmarkFolder targetBookmarkFolder = (BookmarkFolder) targetBookmark;
			BookmarkId bookmarkFolderId = targetBookmarkFolder.getId();
			BookmarkFolder sourceBookmarkFolder = (BookmarkFolder) sourceTree.getBookmark(bookmarkFolderId);
			if (sourceBookmarkFolder != null) {
				List<BookmarkId> sourceChildren = getBookmarkChildrenIds(sourceTree, bookmarkFolderId,
						hasParent(bookmarksTreeModifier.getCurrentTree(), bookmarkFolderId)
								.and(hasParent(sourceTree, bookmarkFolderId)));
				List<BookmarkId> targetChildren = getBookmarkChildrenIds(bookmarksTreeModifier.getCurrentTree(),
						bookmarkFolderId, hasParent(bookmarksTreeModifier.getCurrentTree(), bookmarkFolderId)
								.and(hasParent(sourceTree, bookmarkFolderId)));
				if (!sourceChildren.equals(targetChildren)) {
					reorderBookmarkFolderChildren(bookmarksTreeModifier, bookmarkFolderId, sourceChildren,
							targetChildren);
				}
			}
		}
	}

	private Bookmark getSourceBookmark(BookmarkId bookmarkId) {
//		if (BookmarksTreeUtils.isBookmarkUnder(sourceTree, bookmarkId, sourceRootBookmarkId)) {
			return sourceTree.getBookmark(bookmarkId);
//		} else {
//			return null;
//		}
	}
	
	private void reorderBookmarkFolderChildren(IBookmarksTreeModifier bookmarksTreeModifier, BookmarkId parentId,
			List<BookmarkId> sourceChildren, List<BookmarkId> targetChildren) {
		for (Move<BookmarkId> move : MinimumMovesSolver.getMinimumMoves(targetChildren, sourceChildren)) {
			BookmarkId existingBookmarkId = targetChildren.get(move.getTo());
			if (move.getFrom() > move.getTo()) {
				bookmarksTreeModifier.moveBefore(Arrays.asList(move.getElement()), parentId, existingBookmarkId);
			} else {
				bookmarksTreeModifier.moveAfter(Arrays.asList(move.getElement()), parentId, existingBookmarkId);
			}
			BookmarkId id = targetChildren.remove(move.getFrom());
			targetChildren.add(move.getTo(), id);
		}
	}

	private List<BookmarkId> getBookmarkChildrenIds(BookmarksTree bookmarksTree, BookmarkId bookmarkFolderId,
			Predicate<Bookmark> filter) {
		List<BookmarkId> result = new ArrayList<>();
		for (Bookmark bookmark : bookmarksTree.getChildren(bookmarkFolderId)) {
			if (filter.test(bookmark)) {
				result.add(bookmark.getId());
			}
		}
		return result;
	}

	private void handleBookmarksMovedToOtherFolder(IBookmarksTreeModifier bookmarksTreeModifier) {
		BookmarkId folderId = sourceTree.getRootFolder().getId();
		BookmarksTreeIterable postOrderTargetBookmarksTreeIterable = new BookmarksTreeIterable(
				bookmarksTreeModifier.getCurrentTree(), folderId, Algorithm.POST_ORDER);
		for (Bookmark targetBookmark : postOrderTargetBookmarksTreeIterable) {
			BookmarkId bookmarkId = targetBookmark.getId();
			Bookmark sourceBookmark = getSourceBookmark(bookmarkId);
			if (sourceBookmark != null) {
				BookmarkFolder targetParent = bookmarksTreeModifier.getCurrentTree().getParentBookmark(bookmarkId);
				BookmarkFolder sourceParent = sourceTree.getParentBookmark(bookmarkId);
				if (sourceParent != null && !sourceParent.getId().equals(targetParent.getId())) {
					// moved to another folder
					Bookmark sourceBookmarkBefore = getBookmarkBefore(sourceTree, sourceBookmark.getId(),
							hasParent(bookmarksTreeModifier.getCurrentTree(), sourceParent.getId()));
					bookmarksTreeModifier.moveAfter(Lists.newArrayList(bookmarkId), sourceParent.getId(),
							sourceBookmarkBefore == null ? null : sourceBookmarkBefore.getId());
				}
			}
		}
	}

	private void handleDeletedOrUpdatedBookmarks(IBookmarksTreeModifier bookmarksTreeModifier) {
		BookmarkId folderId = sourceTree.getRootFolder().getId();
		BookmarksTreeIterable preOrderTargetBookmarksTreeIterable = new BookmarksTreeIterable(
				bookmarksTreeModifier.getCurrentTree(), folderId, Algorithm.PRE_ORDER);
		for (Bookmark targetBookmark : preOrderTargetBookmarksTreeIterable) {
			BookmarkId bookmarkId = targetBookmark.getId();
			Bookmark sourceBookmark = getSourceBookmark(bookmarkId);
			if (sourceBookmark == null) {
				// deleted bookmark
				bookmarksTreeModifier.deleteBookmark(bookmarkId, true);
			} else {
				// update properties (does not change the tree if they were not
				// modified)
				bookmarksTreeModifier.setProperties(bookmarkId, sourceBookmark.getProperties());
			}
		}
	}

	private void handleNewBookmarks(IBookmarksTreeModifier bookmarksTreeModifier) {
		BookmarkId folderId = sourceTree.getRootFolder().getId();
		// by using pre_order, we make sure parent are added before children
		BookmarksTreeIterable preOrderSourceBookmarksTreeIterable = new BookmarksTreeIterable(sourceTree, folderId,
				Algorithm.PRE_ORDER);
		for (Bookmark sourceBookmark : preOrderSourceBookmarksTreeIterable) {
			BookmarkId bookmarkId = sourceBookmark.getId();
			Bookmark targetBookmark = bookmarksTreeModifier.getCurrentTree().getBookmark(bookmarkId);
			if (targetBookmark == null) {
				// added bookmark
				BookmarkFolder sourceParent = sourceTree.getParentBookmark(bookmarkId);
				Bookmark sourceBookmarkBefore = getBookmarkBefore(sourceTree, sourceBookmark.getId(),
						hasParent(bookmarksTreeModifier.getCurrentTree(), sourceParent.getId()));
				bookmarksTreeModifier.addBookmarksAfter(sourceParent.getId(),
						sourceBookmarkBefore == null ? null : sourceBookmarkBefore.getId(),
						Lists.newArrayList(sourceBookmark));
			}
		}
	}

	private Predicate<Bookmark> hasParent(BookmarksTree bookmarksTree, BookmarkId parentId) {
		return b -> bookmarksTree.getBookmark(b.getId()) != null
				&& parentId.equals(bookmarksTree.getParentBookmark(b.getId()).getId());
	}

	private Bookmark getBookmarkBefore(BookmarksTree bookmarksTree, BookmarkId bookmarkId, Predicate<Bookmark> filter) {
		Bookmark bookmark = bookmarksTree.getBookmark(bookmarkId);
		if (bookmark == null) {
			return null;
		}
		BookmarkFolder parentFolder = bookmarksTree.getParentBookmark(bookmarkId);
		if (parentFolder == null) {
			return null;
		}
		List<Bookmark> children = bookmarksTree.getChildren(parentFolder.getId());
		int index = children.indexOf(bookmark);
		if (index == 0 || index == -1) {
			return null;
		}
		for (int i = index - 1; i >= 0; i--) {
			Bookmark child = children.get(i);
			if (filter.test(child)) {
				return child;
			}
		}
		return null;
	}

}
