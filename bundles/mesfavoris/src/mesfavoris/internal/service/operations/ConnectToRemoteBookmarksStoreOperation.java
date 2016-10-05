package mesfavoris.internal.service.operations;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import mesfavoris.BookmarksException;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.persistence.IBookmarksDirtyStateTracker;
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
			IBookmarksDirtyStateTracker bookmarksDirtyStateTracker) {
		this.remoteBookmarksStoreManager = remoteBookmarksStoreManager;
		this.refreshRemoteFolderOperation = new RefreshRemoteFolderOperation(bookmarkDatabase,
				remoteBookmarksStoreManager, bookmarksDirtyStateTracker);
	}

	public void connectToRemoteBookmarksStore(String storeId, IProgressMonitor monitor) throws BookmarksException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Connecting to remote bookmarks store", 100);
		try {
			IRemoteBookmarksStore store = remoteBookmarksStoreManager.getRemoteBookmarksStore(storeId);
			if (store == null) {
				throw new BookmarksException("Unknown store id");
			}
			store.connect(subMonitor.newChild(50));
			refreshRemoteFolderOperation.refresh(store.getDescriptor().getId(), subMonitor.newChild(50));
		} catch (IOException e) {
			throw new BookmarksException("Could not connect to remote bookmarks store", e);
		}
	}

}
