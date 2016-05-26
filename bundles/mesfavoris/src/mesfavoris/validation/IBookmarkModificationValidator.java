package mesfavoris.validation;

import org.eclipse.core.runtime.IStatus;

import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;

public interface IBookmarkModificationValidator {

	public IStatus validateModification(BookmarksTree bookmarksTree, BookmarkId bookmarkId);
	
}
