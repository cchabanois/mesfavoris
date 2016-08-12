package mesfavoris.internal.remote;

import static mesfavoris.remote.IRemoteBookmarksStore.PROP_BOOKMARK_FOLDER_ID;
import static mesfavoris.remote.IRemoteBookmarksStore.TOPIC_MAPPING_CHANGED;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import mesfavoris.BookmarksException;
import mesfavoris.internal.operations.RefreshRemoteFolderOperation;
import mesfavoris.model.BookmarkId;

/**
 * Event handler that refresh a remote bookmark folder when it changed on the
 * remote store
 * 
 * @author cchabanois
 *
 */
public class RemoteBookmarksTreeChangeEventHandler implements EventHandler {
	private final IEventBroker eventBroker;
	private final RefreshRemoteFolderOperation refreshRemoteFolderOperation;

	public RemoteBookmarksTreeChangeEventHandler(IEventBroker eventBroker,
			RefreshRemoteFolderOperation refreshRemoteFolderOperation) {
		this.eventBroker = eventBroker;
		this.refreshRemoteFolderOperation = refreshRemoteFolderOperation;
	}

	public void subscribe() {
		eventBroker.subscribe(TOPIC_MAPPING_CHANGED, this);
	}

	public void unsubscribe() {
		eventBroker.unsubscribe(this);
	}

	@Override
	public void handleEvent(Event event) {
		final BookmarkId bookmarkFolderId = new BookmarkId((String) event.getProperty(PROP_BOOKMARK_FOLDER_ID));
		new Job("Updating remote bookmark folder") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					refreshRemoteFolderOperation.refresh(bookmarkFolderId, monitor);
					return Status.OK_STATUS;
				} catch (BookmarksException e) {
					return e.getStatus();
				}
			}

		}.schedule();
	}

}
