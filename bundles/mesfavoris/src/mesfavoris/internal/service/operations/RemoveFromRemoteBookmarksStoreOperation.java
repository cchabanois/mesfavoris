package mesfavoris.internal.service.operations;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;

import mesfavoris.BookmarksException;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.remote.IRemoteBookmarksStore;
import mesfavoris.remote.RemoteBookmarksStoreManager;

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
		final IRemoteBookmarksStore store = remoteBookmarksStoreManager.getRemoteBookmarksStore(storeId)
				.orElseThrow(() -> new BookmarksException("Unknown store id"));
		bookmarkDatabase.modify(bookmarksTreeModifier -> {
			try {
				store.remove(bookmarkFolderId, monitor);
			} catch (IOException e) {
				throw new BookmarksException("Could not remove bookmark folder from store", e);
			}
		});

	}

}
