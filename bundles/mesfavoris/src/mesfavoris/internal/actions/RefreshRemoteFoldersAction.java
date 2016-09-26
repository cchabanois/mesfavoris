package mesfavoris.internal.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;

import mesfavoris.BookmarksException;
import mesfavoris.BookmarksPlugin;
import mesfavoris.internal.service.operations.RefreshRemoteFolderOperation;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.persistence.IBookmarksDatabaseDirtyStateTracker;
import mesfavoris.remote.RemoteBookmarksStoreManager;

public class RefreshRemoteFoldersAction extends Action {
	private final BookmarkDatabase bookmarkDatabase;
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;
	private final IBookmarksDatabaseDirtyStateTracker bookmarksDatabaseDirtyStateTracker;
	
	public RefreshRemoteFoldersAction(BookmarkDatabase bookmarkDatabase, RemoteBookmarksStoreManager remoteBookmarksStoreManager, IBookmarksDatabaseDirtyStateTracker bookmarksDatabaseDirtyStateTracker) {
		super("&Refresh", BookmarksPlugin.imageDescriptorFromPlugin(BookmarksPlugin.PLUGIN_ID, "icons/refresh.gif"));
		setToolTipText("Refresh bookmarks");	
		this.bookmarkDatabase = bookmarkDatabase;
		this.remoteBookmarksStoreManager = remoteBookmarksStoreManager;
		this.bookmarksDatabaseDirtyStateTracker = bookmarksDatabaseDirtyStateTracker;
	}
	
	public void run() {
		RefreshRemoteFolderOperation operation = new RefreshRemoteFolderOperation(bookmarkDatabase,
				remoteBookmarksStoreManager,
				bookmarksDatabaseDirtyStateTracker);
		ConnectToBookmarksStoreJob job = new ConnectToBookmarksStoreJob(operation);
		job.setUser(true);
		job.schedule();
	}

	private static final class ConnectToBookmarksStoreJob extends Job {
		private final RefreshRemoteFolderOperation operation;

		public ConnectToBookmarksStoreJob(RefreshRemoteFolderOperation operation) {
			super("Refreshing remote bookmark folders");
			this.operation = operation;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				operation.refresh(monitor);
				return Status.OK_STATUS;
			} catch (BookmarksException e) {
				return e.getStatus();
			}

		}

	}

	
	
}
