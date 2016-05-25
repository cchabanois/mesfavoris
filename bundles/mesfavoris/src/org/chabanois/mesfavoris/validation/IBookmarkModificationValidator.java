package org.chabanois.mesfavoris.validation;

import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.model.BookmarksTree;
import org.eclipse.core.runtime.IStatus;

public interface IBookmarkModificationValidator {

	public IStatus validateModification(BookmarksTree bookmarksTree, BookmarkId bookmarkId);
	
}
