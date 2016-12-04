package mesfavoris.internal.validation;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.model.modification.BookmarksModification;
import mesfavoris.model.modification.IBookmarksModificationValidator;

public class AcceptAllBookmarksModificationValidator implements IBookmarksModificationValidator {

	@Override
	public IStatus validateModification(BookmarksModification bookmarksModification) {
		return Status.OK_STATUS;
	}

	@Override
	public IStatus validateModification(BookmarksTree bookmarksTree, BookmarkId bookmarkId) {
		return Status.OK_STATUS;
	}

}
