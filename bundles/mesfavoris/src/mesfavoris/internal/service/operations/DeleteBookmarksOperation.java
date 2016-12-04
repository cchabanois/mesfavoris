package mesfavoris.internal.service.operations;

import java.util.List;

import mesfavoris.BookmarksException;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;

public class DeleteBookmarksOperation {
	private final BookmarkDatabase bookmarkDatabase;

	public DeleteBookmarksOperation(BookmarkDatabase bookmarkDatabase) {
		this.bookmarkDatabase = bookmarkDatabase;
	}

	public void deleteBookmarks(final List<BookmarkId> selection) throws BookmarksException {
		bookmarkDatabase.modify(bookmarksTreeModifier -> {
			for (BookmarkId bookmarkId : selection) {
				BookmarkFolder bookmarkFolder = bookmarksTreeModifier.getCurrentTree().getParentBookmark(bookmarkId);
				if (bookmarkFolder == null) {
					return;
				}
				bookmarksTreeModifier.deleteBookmark(bookmarkId, true);
			}
		});
	}

}
