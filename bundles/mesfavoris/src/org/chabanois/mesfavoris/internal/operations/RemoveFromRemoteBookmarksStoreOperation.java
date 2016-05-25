package org.chabanois.mesfavoris.internal.operations;

import java.io.IOException;

import org.chabanois.mesfavoris.BookmarksException;
import org.chabanois.mesfavoris.model.BookmarkDatabase;
import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.remote.IRemoteBookmarksStore;
import org.chabanois.mesfavoris.remote.RemoteBookmarksStoreManager;
import org.eclipse.core.runtime.IProgressMonitor;

public class RemoveFromRemoteBookmarksStoreOperation {
	private final BookmarkDatabase bookmarkDatabase;
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;

	public RemoveFromRemoteBookmarksStoreOperation(BookmarkDatabase bookmarkDatabase,
			RemoteBookmarksStoreManager remoteBookmarksStoreManager) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.remoteBookmarksStoreManager = remoteBookmarksStoreManager;
	}

	public void removeFromRemoteBookmarksStore(String storeId, final BookmarkId bookmarkFolderId,
			final IProgressMonitor monitor) throws BookmarksException {
		final IRemoteBookmarksStore store = remoteBookmarksStoreManager.getRemoteBookmarksStore(storeId);
		if (store == null) {
			throw new BookmarksException("Unknown store id");
		}
		bookmarkDatabase.modify(bookmarksTreeModifier -> {
			try {
				store.remove(bookmarkFolderId, monitor);
			} catch (IOException e) {
				throw new BookmarksException("Could not remove bookmark folder from store", e);
			}
		});

	}

}
