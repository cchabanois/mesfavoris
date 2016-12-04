package mesfavoris.internal.service.operations;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;

import mesfavoris.BookmarksException;
import mesfavoris.internal.model.merge.BookmarksTreeMerger;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.LockMode;
import mesfavoris.model.OptimisticLockException;
import mesfavoris.persistence.IBookmarksDirtyStateTracker;
import mesfavoris.remote.IRemoteBookmarksStore;
import mesfavoris.remote.IRemoteBookmarksStore.State;
import mesfavoris.remote.RemoteBookmarkFolder;
import mesfavoris.remote.RemoteBookmarksStoreManager;
import mesfavoris.remote.RemoteBookmarksTree;

/**
 * Refresh remote folders in the bookmark database. Load remote bookmarks from
 * remote stores and replace them in the bookmark database.
 * 
 * @author cchabanois
 *
 */
public class RefreshRemoteFolderOperation {
	private final BookmarkDatabase bookmarkDatabase;
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;
	private final IBookmarksDirtyStateTracker bookmarksDirtyStateTracker;

	public RefreshRemoteFolderOperation(BookmarkDatabase bookmarkDatabase,
			RemoteBookmarksStoreManager remoteBookmarksStoreManager,
			IBookmarksDirtyStateTracker bookmarksDirtyStateTracker) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.remoteBookmarksStoreManager = remoteBookmarksStoreManager;
		this.bookmarksDirtyStateTracker = bookmarksDirtyStateTracker;
	}

	public void refresh(IProgressMonitor monitor) throws BookmarksException {
		Collection<IRemoteBookmarksStore> stores = remoteBookmarksStoreManager.getRemoteBookmarksStores();
		List<String> storeIds = stores.stream().filter(store -> store.getState() == State.connected)
				.map(store -> store.getDescriptor().getId()).collect(Collectors.toList());
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Loading bookmark folders from all stores",
				storeIds.size());
		Exception exception = null;
		for (String storeId : storeIds) {
			try {
				refresh(storeId, subMonitor.newChild(1));
			} catch (OperationCanceledException e) {
				throw e;
			} catch (Exception e) {
				exception = e;
			}
		}
		if (exception != null) {
			if (exception instanceof BookmarksException) {
				throw (BookmarksException) exception;
			} else {
				throw (RuntimeException) exception;
			}
		}
	}

	public void refresh(String storeId, IProgressMonitor monitor) throws BookmarksException {
		IRemoteBookmarksStore store = remoteBookmarksStoreManager.getRemoteBookmarksStore(storeId);
		if (store == null) {
			throw new BookmarksException("Remore bookmarks store not found");
		}
		Set<RemoteBookmarkFolder> remoteBookmarkFolders = store.getRemoteBookmarkFolders();
		SubMonitor subMonitor = SubMonitor.convert(monitor,
				"Loading bookmark folders from " + store.getDescriptor().getLabel(), remoteBookmarkFolders.size());
		Exception exception = null;
		for (RemoteBookmarkFolder bookmarkFolder : remoteBookmarkFolders) {
			try {
				refresh(bookmarkFolder.getBookmarkFolderId(), subMonitor.newChild(1));
			} catch (OperationCanceledException e) {
				throw e;
			} catch (Exception e) {
				exception = e;
			}
		}
		if (exception != null) {
			if (exception instanceof BookmarksException) {
				throw (BookmarksException) exception;
			} else {
				throw (RuntimeException) exception;
			}
		}
	}

	public void refresh(BookmarkId bookmarkFolderId, IProgressMonitor monitor) throws BookmarksException {
		RemoteBookmarkFolder remoteBookmarkFolder = remoteBookmarksStoreManager
				.getRemoteBookmarkFolder(bookmarkFolderId);
		if (remoteBookmarkFolder == null) {
			throw new BookmarksException("Not a remote boomark folder");
		}
		String storeId = remoteBookmarkFolder.getRemoteBookmarkStoreId();
		IRemoteBookmarksStore store = remoteBookmarksStoreManager.getRemoteBookmarksStore(storeId);
		do {
			try {
				bookmarkDatabase.modify(LockMode.OPTIMISTIC, (bookmarksTreeModifier) -> {
					if (bookmarksDirtyStateTracker.isDirty()) {
						throw new DirtyBookmarksDatabaseException();
					}
					try {
						RemoteBookmarksTree remoteBookmarksTree = store.load(bookmarkFolderId, monitor);
						// replace existing bookmark folder with remote one
						BookmarksTreeMerger bookmarksTreeMerger = new BookmarksTreeMerger(
								remoteBookmarksTree.getBookmarksTree());
						bookmarksTreeMerger.merge(bookmarksTreeModifier);
					} catch (IOException e) {
						throw new BookmarksException("Could not load remote bookmark folder", e);
					}
				}, /* validateModifications */ false);
				return;
			} catch (OptimisticLockException|DirtyBookmarksDatabaseException e) {
				try {
					// sleep and retry later
					Thread.sleep(200);
				} catch (InterruptedException e1) {
					throw new BookmarksException("Could not load remote bookmark folder", e1);
				}
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
			}
		} while (true);
	}

	private static class DirtyBookmarksDatabaseException extends BookmarksException {
		private static final long serialVersionUID = 6024826805648888249L;

		public DirtyBookmarksDatabaseException() {
			super("Bookmark database is dirty");
		}

	}
	
}
