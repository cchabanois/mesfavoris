package org.chabanois.mesfavoris.internal.operations;

import java.util.List;

import org.chabanois.mesfavoris.BookmarksException;
import org.chabanois.mesfavoris.internal.model.utils.BookmarksTreeUtils;
import org.chabanois.mesfavoris.model.BookmarkDatabase;
import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.model.BookmarksTree;
import org.chabanois.mesfavoris.validation.IBookmarkModificationValidator;

public class CutBookmarkOperation {
	private final BookmarkDatabase bookmarkDatabase;
	private final IBookmarkModificationValidator bookmarkModificationValidator;
	
	public CutBookmarkOperation(BookmarkDatabase bookmarkDatabase, IBookmarkModificationValidator bookmarkModificationValidator) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.bookmarkModificationValidator = bookmarkModificationValidator;
	}

	public void cutToClipboard(List<BookmarkId> selection)
			throws BookmarksException {
		CopyBookmarkOperation copyBookmarkOperation = new CopyBookmarkOperation();
		copyBookmarkOperation.copyToClipboard(bookmarkDatabase.getBookmarksTree(),selection);
		DeleteBookmarksOperation deleteBookmarksOperation = new DeleteBookmarksOperation(bookmarkDatabase, bookmarkModificationValidator);
		deleteBookmarksOperation.deleteBookmarks(selection);
	}

	public boolean hasDuplicatedBookmarksInSelection(BookmarksTree bookmarksTree, List<BookmarkId> selection) {
		return selection.size() != BookmarksTreeUtils.getUnduplicatedBookmarks(bookmarksTree, selection).size();
	}
	
}
