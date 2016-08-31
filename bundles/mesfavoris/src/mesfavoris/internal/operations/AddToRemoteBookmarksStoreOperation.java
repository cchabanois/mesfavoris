package mesfavoris.internal.operations;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;

import mesfavoris.BookmarksException;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.remote.IRemoteBookmarksStore;
import mesfavoris.remote.IRemoteBookmarksStore.State;
import mesfavoris.remote.RemoteBookmarksStoreManager;

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
			store.add(bookmarksTree, bookmarkFolderId, monitor);
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
