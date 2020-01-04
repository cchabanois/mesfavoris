package mesfavoris.gdrive.changes;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Change;

import mesfavoris.gdrive.StatusHelper;
import mesfavoris.gdrive.connection.GDriveConnectionManager;
import mesfavoris.gdrive.connection.IConnectionListener;
import mesfavoris.gdrive.mappings.BookmarkMapping;
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
	private final Supplier<Duration> pollDelayProvider;
	private final IBookmarkMappings bookmarkMappings;
	private final ListenerList<IBookmarksFileChangeListener> listenerList = new ListenerList<>();
	private final AtomicBoolean closed = new AtomicBoolean(false);
	
	public BookmarksFileChangeManager(GDriveConnectionManager gdriveConnectionManager,
			IBookmarkMappings bookmarkMappings) {
		this(gdriveConnectionManager, bookmarkMappings, ()->DEFAULT_POLL_DELAY);
	}
	
	public BookmarksFileChangeManager(GDriveConnectionManager gdriveConnectionManager,
			IBookmarkMappings bookmarkMappings, Supplier<Duration> pollDelayProvider) {
		this.gdriveConnectionManager = gdriveConnectionManager;
		this.bookmarkMappings = bookmarkMappings;
		this.pollDelayProvider = pollDelayProvider;
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
		listenerList.add(listener);
	}

	public void removeListener(IBookmarksFileChangeListener listener) {
		listenerList.remove(listener);
	}

	private void fireBookmarksFileChanged(BookmarkId bookmarkFolderId, Change change) {
		for (IBookmarksFileChangeListener listener : listenerList) {
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
			super("Get changes from Google Drive");
			setSystem(true);
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
					Optional<BookmarkMapping> bookmarkMapping = bookmarkMappings.getMapping(change.getFileId());
					if (bookmarkMapping.isPresent()) {
						fireBookmarksFileChanged(bookmarkMapping.get().getBookmarkFolderId(), change);
					}
				}
				if (changes.size() > 0) {
					startChangeId = changes.get(changes.size() - 1).getId() + 1;
				}
				return Status.OK_STATUS;
			} catch (UnknownHostException|SocketTimeoutException e) {
				gdriveConnectionManager.disconnect(new NullProgressMonitor());
				return Status.OK_STATUS;
			} catch (IOException e) {
				StatusHelper.logWarn("Could not get remote bookmark changes",e);
				// Do not display a dialog
				return Status.OK_STATUS;
			} finally {
				schedule(pollDelayProvider.get().toMillis());
			}
		}

		@Override
		public boolean shouldSchedule() {
			return gdriveConnectionManager.getState() == State.connected && !closed.get();
		}

	}

}
