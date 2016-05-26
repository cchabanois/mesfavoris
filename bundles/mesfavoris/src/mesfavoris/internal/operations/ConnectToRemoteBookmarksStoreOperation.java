package mesfavoris.internal.operations;

import java.io.IOException;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import mesfavoris.BookmarksException;
import mesfavoris.internal.model.merge.BookmarksTreeMerger;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.remote.IRemoteBookmarksStore;
import mesfavoris.remote.RemoteBookmarksStoreManager;
import mesfavoris.remote.RemoteBookmarksTree;

public class ConnectToRemoteBookmarksStoreOperation {
	private final BookmarkDatabase bookmarkDatabase;
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;

	public ConnectToRemoteBookmarksStoreOperation(BookmarkDatabase bookmarkDatabase,
			RemoteBookmarksStoreManager remoteBookmarksStoreManager) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.remoteBookmarksStoreManager = remoteBookmarksStoreManager;
	}

	public void connectToRemoteBookmarksStore(String storeId, IProgressMonitor monitor) throws BookmarksException {
		monitor.beginTask("Connecting to remote bookmarks store", 100);
		try {
			IRemoteBookmarksStore store = remoteBookmarksStoreManager.getRemoteBookmarksStore(storeId);
			if (store == null) {
				throw new BookmarksException("Unknown store id");
			}
			store.connect(new SubProgressMonitor(monitor, 80));
			loadRemoteBookmarkFolders(store, new SubProgressMonitor(monitor, 20));
		} catch (IOException e) {
			throw new BookmarksException("Could not connect to remote bookmarks store", e);
		} finally {
			monitor.done();
		}
	}

	private void loadRemoteBookmarkFolders(IRemoteBookmarksStore store, IProgressMonitor monitor)
			throws BookmarksException {
		Set<BookmarkId> remoteBookmarkFolderIds = store.getRemoteBookmarkFolderIds();
		monitor.beginTask("Loading bookmark folders", remoteBookmarkFolderIds.size());
		try {
			for (BookmarkId bookmarkFolderId : remoteBookmarkFolderIds) {
				RemoteBookmarksTree remoteBookmarksTree = store.load(bookmarkFolderId,
						new SubProgressMonitor(monitor, 1));
				replaceBookmark(remoteBookmarksTree);
			}
		} catch (IOException e) {
			throw new BookmarksException("Could not load remote bookmark folders", e);
		} finally {
			monitor.done();
		}
	}

	private void replaceBookmark(final RemoteBookmarksTree remoteBookmarksTree) throws BookmarksException {
		bookmarkDatabase.modify(bookmarksTreeModifier -> {
			BookmarksTreeMerger bookmarksTreeMerger = new BookmarksTreeMerger(remoteBookmarksTree.getBookmarksTree());
			bookmarksTreeMerger.merge(bookmarksTreeModifier);
		});

	}
}
