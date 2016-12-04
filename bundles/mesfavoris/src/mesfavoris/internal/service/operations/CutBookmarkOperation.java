package mesfavoris.internal.service.operations;

import java.util.List;

import mesfavoris.BookmarksException;
import mesfavoris.internal.model.utils.BookmarksTreeUtils;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;

public class CutBookmarkOperation {
	private final BookmarkDatabase bookmarkDatabase;
	
	public CutBookmarkOperation(BookmarkDatabase bookmarkDatabase) {
		this.bookmarkDatabase = bookmarkDatabase;
	}

	public void cutToClipboard(List<BookmarkId> selection)
			throws BookmarksException {
		CopyBookmarkOperation copyBookmarkOperation = new CopyBookmarkOperation();
		copyBookmarkOperation.copyToClipboard(bookmarkDatabase.getBookmarksTree(),selection);
		DeleteBookmarksOperation deleteBookmarksOperation = new DeleteBookmarksOperation(bookmarkDatabase);
		deleteBookmarksOperation.deleteBookmarks(selection);
	}

	public boolean hasDuplicatedBookmarksInSelection(BookmarksTree bookmarksTree, List<BookmarkId> selection) {
		return selection.size() != BookmarksTreeUtils.getUnduplicatedBookmarks(bookmarksTree, selection).size();
	}
	
}
