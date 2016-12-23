package mesfavoris.internal.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;

import mesfavoris.BookmarksException;
import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.service.operations.RefreshRemoteFolderOperation;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.persistence.IBookmarksDirtyStateTracker;
import mesfavoris.remote.RemoteBookmarksStoreManager;

public class RefreshRemoteFoldersAction extends Action {
	private static final String ID = "mesfavoris.view.refresh";
	private final BookmarkDatabase bookmarkDatabase;
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;
	private final IBookmarksDirtyStateTracker bookmarksDirtyStateTracker;

	public RefreshRemoteFoldersAction(BookmarkDatabase bookmarkDatabase,
			RemoteBookmarksStoreManager remoteBookmarksStoreManager,
			IBookmarksDirtyStateTracker bookmarksDirtyStateTracker) {
		super("&Refresh", BookmarksPlugin.imageDescriptorFromPlugin(BookmarksPlugin.PLUGIN_ID, "icons/refresh.gif"));
		setToolTipText("Refresh bookmarks");
		setId(ID);
		this.bookmarkDatabase = bookmarkDatabase;
		this.remoteBookmarksStoreManager = remoteBookmarksStoreManager;
		this.bookmarksDirtyStateTracker = bookmarksDirtyStateTracker;
	}

	public void run() {
		RefreshRemoteFolderOperation operation = new RefreshRemoteFolderOperation(bookmarkDatabase,
				remoteBookmarksStoreManager, bookmarksDirtyStateTracker);
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
