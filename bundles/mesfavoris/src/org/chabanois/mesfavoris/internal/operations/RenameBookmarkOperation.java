package org.chabanois.mesfavoris.internal.operations;

import org.chabanois.mesfavoris.BookmarksException;
import org.chabanois.mesfavoris.model.Bookmark;
import org.chabanois.mesfavoris.model.BookmarkDatabase;
import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.validation.IBookmarkModificationValidator;
import org.eclipse.core.runtime.IStatus;

public class RenameBookmarkOperation {
	private final BookmarkDatabase bookmarkDatabase;
	private final IBookmarkModificationValidator bookmarkModificationValidator;

	public RenameBookmarkOperation(BookmarkDatabase bookmarkDatabase,
			IBookmarkModificationValidator bookmarkModificationValidator) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.bookmarkModificationValidator = bookmarkModificationValidator;
	}

	public void renameBookmark(BookmarkId bookmarkId, String newName) throws BookmarksException {
		bookmarkDatabase.modify(bookmarksTreeModifier -> {
			IStatus status = bookmarkModificationValidator.validateModification(bookmarksTreeModifier.getCurrentTree(),
					bookmarkId);
			if (!status.isOK()) {
				throw new BookmarksException(status);
			}
			bookmarksTreeModifier.setPropertyValue(bookmarkId, Bookmark.PROPERTY_NAME, newName);
		});
	}

}
