package mesfavoris.internal.operations;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import mesfavoris.BookmarksException;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.persistence.IBookmarksDatabaseDirtyStateTracker;
import mesfavoris.remote.IRemoteBookmarksStore;
import mesfavoris.remote.RemoteBookmarksStoreManager;

/**
 * Connect to a remote bookmark store
 * 
 * @author cchabanois
 *
 */
public class ConnectToRemoteBookmarksStoreOperation {
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;
	private final RefreshRemoteFolderOperation refreshRemoteFolderOperation;

	public ConnectToRemoteBookmarksStoreOperation(BookmarkDatabase bookmarkDatabase,
			RemoteBookmarksStoreManager remoteBookmarksStoreManager,
			IBookmarksDatabaseDirtyStateTracker bookmarksDatabaseDirtyStateTracker) {
		this.remoteBookmarksStoreManager = remoteBookmarksStoreManager;
		this.refreshRemoteFolderOperation = new RefreshRemoteFolderOperation(bookmarkDatabase,
				remoteBookmarksStoreManager, bookmarksDatabaseDirtyStateTracker);
	}

	public void connectToRemoteBookmarksStore(String storeId, IProgressMonitor monitor) throws BookmarksException {
		monitor.beginTask("Connecting to remote bookmarks store", 100);
		try {
			IRemoteBookmarksStore store = remoteBookmarksStoreManager.getRemoteBookmarksStore(storeId);
			if (store == null) {
				throw new BookmarksException("Unknown store id");
			}
			store.connect(new SubProgressMonitor(monitor, 80));
			refreshRemoteFolderOperation.refresh(store.getDescriptor().getId(), new SubProgressMonitor(monitor, 20));
		} catch (IOException e) {
			throw new BookmarksException("Could not connect to remote bookmarks store", e);
		} finally {
			monitor.done();
		}
	}

}
