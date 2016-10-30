package mesfavoris.internal.service.operations;

import java.util.List;

import mesfavoris.BookmarksException;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.validation.IBookmarkModificationValidator;

public class DeleteBookmarksOperation {
	private final BookmarkDatabase bookmarkDatabase;
	private final IBookmarkModificationValidator bookmarkModificationValidator;

	public DeleteBookmarksOperation(BookmarkDatabase bookmarkDatabase,
			IBookmarkModificationValidator bookmarkModificationValidator) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.bookmarkModificationValidator = bookmarkModificationValidator;
	}

	public void deleteBookmarks(final List<BookmarkId> selection) throws BookmarksException {
		bookmarkDatabase.modify(bookmarksTreeModifier -> {
			for (BookmarkId bookmarkId : selection) {
				BookmarkFolder bookmarkFolder = bookmarksTreeModifier.getCurrentTree().getParentBookmark(bookmarkId);
				if (bookmarkFolder == null) {
					return;
				}
				if (bookmarkModificationValidator
						.validateModification(bookmarksTreeModifier.getCurrentTree(), bookmarkFolder.getId()).isOK()) {
					bookmarksTreeModifier.deleteBookmark(bookmarkId, true);
				}
			}
		});
	}

}
