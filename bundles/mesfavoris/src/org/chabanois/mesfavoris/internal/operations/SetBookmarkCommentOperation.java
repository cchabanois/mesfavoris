package org.chabanois.mesfavoris.internal.operations;

import org.chabanois.mesfavoris.BookmarksException;
import org.chabanois.mesfavoris.model.Bookmark;
import org.chabanois.mesfavoris.model.BookmarkDatabase;
import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.validation.IBookmarkModificationValidator;
import org.eclipse.core.runtime.IStatus;

public class SetBookmarkCommentOperation {
	private final BookmarkDatabase bookmarkDatabase;
	private final IBookmarkModificationValidator bookmarkModificationValidator;

	public SetBookmarkCommentOperation(BookmarkDatabase bookmarkDatabase,
			IBookmarkModificationValidator bookmarkModificationValidator) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.bookmarkModificationValidator = bookmarkModificationValidator;
	}

	public void setComment(final BookmarkId bookmarkId, final String comment) throws BookmarksException {
		bookmarkDatabase.modify(bookmarksTreeModifier -> {
			IStatus status = bookmarkModificationValidator.validateModification(bookmarksTreeModifier.getCurrentTree(),
					bookmarkId);
			if (!status.isOK()) {
				throw new BookmarksException(status);
			}
			bookmarksTreeModifier.setPropertyValue(bookmarkId, Bookmark.PROPERTY_COMMENT, comment);
		});
	}

}
