package org.chabanois.mesfavoris.internal.propertytester;

import org.chabanois.mesfavoris.BookmarksPlugin;
import org.chabanois.mesfavoris.internal.model.utils.BookmarksTreeUtils;
import org.chabanois.mesfavoris.model.Bookmark;
import org.chabanois.mesfavoris.model.BookmarkDatabase;
import org.chabanois.mesfavoris.remote.RemoteBookmarksStoreManager;
import org.chabanois.mesfavoris.validation.BookmarkModificationValidator;
import org.chabanois.mesfavoris.validation.IBookmarkModificationValidator;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.IStatus;

public class BookmarksPropertyTester extends PropertyTester {
	private final BookmarkDatabase bookmarkDatabase;
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;
	private final IBookmarkModificationValidator bookmarkModificationValidator;

	public BookmarksPropertyTester() {
		this(BookmarksPlugin.getBookmarkDatabase(), BookmarksPlugin.getRemoteBookmarksStoreManager());
	}

	public BookmarksPropertyTester(BookmarkDatabase bookmarkDatabase,
			RemoteBookmarksStoreManager remoteBookmarksStoreManager) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.remoteBookmarksStoreManager = remoteBookmarksStoreManager;
		this.bookmarkModificationValidator = new BookmarkModificationValidator(remoteBookmarksStoreManager);
	}

	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (!(receiver instanceof Bookmark)) {
			return false;
		}
		Bookmark bookmark = (Bookmark) receiver;
		if ("canBeModified".equals(property)) {
			return canBeModified(bookmark);
		}
		if ("isUnderRemoteBookmarkFolder".equals(property)) {
			return isUnderRemoteBookmarkFolder(bookmark);
		}
		return false;
	}

	private boolean isUnderRemoteBookmarkFolder(Bookmark bookmark) {
		return remoteBookmarksStoreManager.getRemoteBookmarkFolderContaining(bookmarkDatabase.getBookmarksTree(),
				bookmark.getId()) != null;
	}

	private boolean canBeModified(final Bookmark bookmark) {
		IStatus status = bookmarkModificationValidator.validateModification(bookmarkDatabase.getBookmarksTree(),
				bookmark.getId());
		return status.isOK();
	}

}
