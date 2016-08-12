package mesfavoris.gdrive.changes;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Change;

import mesfavoris.BookmarksException;
import mesfavoris.gdrive.StatusHelper;
import mesfavoris.gdrive.connection.GDriveConnectionManager;
import mesfavoris.gdrive.connection.IConnectionListener;
import mesfavoris.gdrive.mappings.IBookmarkMappings;
import mesfavoris.gdrive.operations.GetChangesOperation;
import mesfavoris.model.BookmarkId;
import mesfavoris.remote.IRemoteBookmarksStore.State;

/**
 * Listen to changes to bookmark files
 * 
 * @author cchabanois
 *
 */
public class BookmarksFileChangeManager {
	public final static Duration DEFAULT_POLL_DELAY = Duration.ofSeconds(30);
	private final GDriveConnectionManager gdriveConnectionManager;
	private final IConnectionListener connectionListener = new ConnectionListener();
	private final BookmarksFileChangeJob job = new BookmarksFileChangeJob();
	private final Duration pollDelay;
	private final IBookmarkMappings bookmarkMappings;
	private final ListenerList connectionListenerList = new ListenerList();
	private final AtomicBoolean closed = new AtomicBoolean(false);
	
	public BookmarksFileChangeManager(GDriveConnectionManager gdriveConnectionManager,
			IBookmarkMappings bookmarkMappings) {
		this(gdriveConnectionManager, bookmarkMappings, DEFAULT_POLL_DELAY);
	}
	
	public BookmarksFileChangeManager(GDriveConnectionManager gdriveConnectionManager,
			IBookmarkMappings bookmarkMappings, Duration polldelay) {
		this.gdriveConnectionManager = gdriveConnectionManager;
		this.bookmarkMappings = bookmarkMappings;
		this.pollDelay = polldelay;
	}

	public void init() {
		gdriveConnectionManager.addConnectionListener(connectionListener);
		// won't be scheduled if not connected
		job.schedule();
	}

	public void close() {
		gdriveConnectionManager.removeConnectionListener(connectionListener);
		job.cancel();
		closed.set(true);
	}

	public void addListener(IBookmarksFileChangeListener listener) {
		connectionListenerList.add(listener);
	}

	public void removeListener(IBookmarksFileChangeListener listener) {
		connectionListenerList.remove(listener);
	}

	private void fireBookmarksFileChanged(BookmarkId bookmarkFolderId, Change change) {
		Object[] listeners = connectionListenerList.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			final IBookmarksFileChangeListener listener = (IBookmarksFileChangeListener) listeners[i];
			SafeRunner.run(new ISafeRunnable() {

				public void run() throws Exception {
					listener.bookmarksFileChanged(bookmarkFolderId, change);
				}

				public void handleException(Throwable exception) {
					StatusHelper.logError("Error in bookmarks file change listener", exception);
				}
			});
		}
	}

	private class ConnectionListener implements IConnectionListener {

		@Override
		public void connected() {
			job.schedule();
		}

		@Override
		public void disconnected() {
			job.cancel();
		}

	}

	private class BookmarksFileChangeJob extends Job {
		private Long startChangeId;

		public BookmarksFileChangeJob() {
			super("Get changes from GDrive");
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				Drive drive = gdriveConnectionManager.getDrive();
				if (drive == null) {
					return Status.OK_STATUS;
				}
				GetChangesOperation operation = new GetChangesOperation(drive);
				if (startChangeId == null) {
					startChangeId = operation.getLargestChangeId() + 1;
				}
				List<Change> changes = operation.getChanges(startChangeId);
				for (Change change : changes) {
					BookmarkId bookmarkFolderId = bookmarkMappings.getBookmarkFolderId(change.getFileId());
					if (bookmarkFolderId != null) {
						fireBookmarksFileChanged(bookmarkFolderId, change);
					}
				}
				if (changes.size() > 0) {
					startChangeId = changes.get(changes.size() - 1).getId() + 1;
				}
				return Status.OK_STATUS;
			} catch (IOException e) {
				return new BookmarksException("Could not get remote bookmark changes", e).getStatus();
			} finally {
				schedule(pollDelay.toMillis());
			}
		}

		@Override
		public boolean shouldSchedule() {
			return gdriveConnectionManager.getState() == State.connected && !closed.get();
		}

	}

}
