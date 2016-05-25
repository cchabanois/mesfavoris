package org.chabanois.mesfavoris.internal.operations;

import java.util.List;

import org.chabanois.mesfavoris.BookmarksException;
import org.chabanois.mesfavoris.model.BookmarkDatabase;
import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.validation.IBookmarkModificationValidator;

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
				if (bookmarkModificationValidator
						.validateModification(bookmarksTreeModifier.getCurrentTree(), bookmarkId).isOK()) {
					bookmarksTreeModifier.deleteBookmark(bookmarkId, true);
				}
			}
		});
	}

}
