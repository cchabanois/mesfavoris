package mesfavoris.model.modification;

import org.eclipse.core.runtime.IStatus;

import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;

public interface IBookmarksModificationValidator {

	/**
	 * Validate that the given modification is allowed
	 * 
	 * @param bookmarksModification
	 * @return
	 */
	IStatus validateModification(BookmarksModification bookmarksModification);

	/**
	 * Check if given bookmark can be modified
	 * 
	 * @param bookmarksTree
	 * @param bookmarkId
	 * @return
	 */
	IStatus validateModification(BookmarksTree bookmarksTree, BookmarkId bookmarkId);

}