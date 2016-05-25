package org.chabanois.mesfavoris.internal.operations;

import java.io.IOException;

import org.chabanois.mesfavoris.BookmarksException;
import org.chabanois.mesfavoris.model.BookmarkDatabase;
import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.model.BookmarksTree;
import org.chabanois.mesfavoris.remote.IRemoteBookmarksStore;
import org.chabanois.mesfavoris.remote.RemoteBookmarksStoreManager;
import org.chabanois.mesfavoris.remote.IRemoteBookmarksStore.State;
import org.chabanois.mesfavoris.remote.RemoteBookmarksTree;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Add a folder to a remote bookmarks store
 * 
 * @author cchabanois
 */
public class AddToRemoteBookmarksStoreOperation {
	private final BookmarkDatabase bookmarkDatabase;
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;

	public AddToRemoteBookmarksStoreOperation(BookmarkDatabase bookmarkDatabase,
			RemoteBookmarksStoreManager remoteBookmarksStoreManager) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.remoteBookmarksStoreManager = remoteBookmarksStoreManager;
	}

	public void addToRemoteBookmarksStore(String storeId, final BookmarkId bookmarkFolderId,
			final IProgressMonitor monitor) throws BookmarksException {
		final IRemoteBookmarksStore store = remoteBookmarksStoreManager.getRemoteBookmarksStore(storeId);
		if (store == null) {
			throw new BookmarksException("Unknown store id");
		}

		if (!canAddToRemoteBookmarkStore(store, bookmarkFolderId)) {
			throw new BookmarksException("Could not add bookmark folder to remote store");
		}
		try {
			BookmarksTree bookmarksTree = bookmarkDatabase.getBookmarksTree();
			RemoteBookmarksTree remoteBookmarksTree = store.add(bookmarksTree, bookmarkFolderId, monitor);
		} catch (IOException e) {
			throw new BookmarksException("Could not add bookmark folder to remote store", e);
		}

	}

	public boolean canAddToRemoteBookmarkStore(String storeId, BookmarkId bookmarkFolderId) {
		final IRemoteBookmarksStore store = remoteBookmarksStoreManager.getRemoteBookmarksStore(storeId);
		if (store == null) {
			return false;
		}
		return canAddToRemoteBookmarkStore(store, bookmarkFolderId);
	}

	private boolean canAddToRemoteBookmarkStore(IRemoteBookmarksStore remoteBookmarksStore,
			BookmarkId bookmarkFolderId) {
		if (remoteBookmarksStore.getState() != State.connected) {
			return false;
		}
		if (isUnderRemoteBookmarksFolder(bookmarkFolderId)) {
			return false;
		}
		return true;
	}

	private boolean isUnderRemoteBookmarksFolder(BookmarkId bookmarkFolderId) {
		return remoteBookmarksStoreManager.getRemoteBookmarkFolderContaining(bookmarkDatabase.getBookmarksTree(),
				bookmarkFolderId) != null;
	}

}
