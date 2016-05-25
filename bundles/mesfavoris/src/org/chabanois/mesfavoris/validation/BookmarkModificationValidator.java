package org.chabanois.mesfavoris.validation;

import org.chabanois.mesfavoris.BookmarksPlugin;
import org.chabanois.mesfavoris.model.Bookmark;
import org.chabanois.mesfavoris.model.BookmarkDatabase;
import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.model.BookmarksTree;
import org.chabanois.mesfavoris.remote.IRemoteBookmarksStore;
import org.chabanois.mesfavoris.remote.IRemoteBookmarksStore.State;
import org.chabanois.mesfavoris.remote.RemoteBookmarkFolder;
import org.chabanois.mesfavoris.remote.RemoteBookmarksStoreManager;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class BookmarkModificationValidator implements IBookmarkModificationValidator {
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;
	
	public BookmarkModificationValidator(RemoteBookmarksStoreManager remoteBookmarksStoreManager) {
		this.remoteBookmarksStoreManager = remoteBookmarksStoreManager;
	}
	
	@Override
	public IStatus validateModification(BookmarksTree bookmarksTree, BookmarkId bookmarkId) {
		Bookmark bookmark = bookmarksTree.getBookmark(bookmarkId);
		if (bookmark == null) {
			return errorStatus("Cannot find bookmark");
		}
		RemoteBookmarkFolder remoteBookmarkFolder = remoteBookmarksStoreManager
				.getRemoteBookmarkFolderContaining(bookmarksTree, bookmarkId);
		if (remoteBookmarkFolder == null) {
			return Status.OK_STATUS;
		}
		IRemoteBookmarksStore remoteBookmarksStore = remoteBookmarksStoreManager.getRemoteBookmarksStore(remoteBookmarkFolder.getRemoteBookmarkStoreId());
		boolean connected = remoteBookmarksStore.getState() == State.connected;
		if (connected) {
			return Status.OK_STATUS;
		} else {
			return errorStatus("Cannot modify bookmark that is under a shared folder not connected");
		}
	}

	private IStatus errorStatus(String message) {
		return new Status(Status.ERROR, BookmarksPlugin.PLUGIN_ID, message);
	}
 	
}
