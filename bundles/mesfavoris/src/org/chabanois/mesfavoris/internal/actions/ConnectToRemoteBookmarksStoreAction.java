package org.chabanois.mesfavoris.internal.actions;

import java.io.IOException;

import org.chabanois.mesfavoris.BookmarksException;
import org.chabanois.mesfavoris.BookmarksPlugin;
import org.chabanois.mesfavoris.internal.operations.ConnectToRemoteBookmarksStoreOperation;
import org.chabanois.mesfavoris.remote.AbstractRemoteBookmarksStore;
import org.chabanois.mesfavoris.remote.IRemoteBookmarksStore;
import org.chabanois.mesfavoris.remote.IRemoteBookmarksStore.State;
import org.chabanois.mesfavoris.remote.IRemoteBookmarksStoreDescriptor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.osgi.service.event.EventHandler;

public class ConnectToRemoteBookmarksStoreAction extends Action implements IWorkbenchAction {
	private final IRemoteBookmarksStore remoteBookmarksStore;
	private final IEventBroker eventBroker;
	private final EventHandler connectionEventHandler;

	public ConnectToRemoteBookmarksStoreAction(IEventBroker eventBroker, IRemoteBookmarksStore store) {
		super("Connect to " + store.getDescriptor().getLabel(), AS_CHECK_BOX);
		this.eventBroker = eventBroker;
		setImageDescriptor(store.getDescriptor().getImageDescriptor());
		this.remoteBookmarksStore = store;
		setChecked(remoteBookmarksStore.getState() == State.connected);
		connectionEventHandler = event -> setChecked(remoteBookmarksStore.getState() == State.connected);
		eventBroker.subscribe(
				AbstractRemoteBookmarksStore.getConnectedTopic(remoteBookmarksStore.getDescriptor().getId()),
				connectionEventHandler);
	}

	@Override
	public void run() {
		if (remoteBookmarksStore.getState() == IRemoteBookmarksStore.State.disconnected) {
			setChecked(false);
			ConnectToBookmarksStoreJob job = new ConnectToBookmarksStoreJob(remoteBookmarksStore.getDescriptor());
			job.setUser(true);
			job.schedule();
		} else {
			setChecked(true);
			DisconnectFromBookmarksStoreJob job = new DisconnectFromBookmarksStoreJob(remoteBookmarksStore);
			job.setUser(true);
			job.schedule();
		}
	}

	@Override
	public void dispose() {
		eventBroker.unsubscribe(connectionEventHandler);
	}

	private static final class ConnectToBookmarksStoreJob extends Job {
		private final IRemoteBookmarksStoreDescriptor storeDescriptor;

		public ConnectToBookmarksStoreJob(IRemoteBookmarksStoreDescriptor storeDescriptor) {
			super("Connecting to " + storeDescriptor.getLabel());
			this.storeDescriptor = storeDescriptor;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				ConnectToRemoteBookmarksStoreOperation operation = new ConnectToRemoteBookmarksStoreOperation(
						BookmarksPlugin.getBookmarkDatabase(), BookmarksPlugin.getRemoteBookmarksStoreManager());
				operation.connectToRemoteBookmarksStore(storeDescriptor.getId(), monitor);
				return Status.OK_STATUS;
			} catch (BookmarksException e) {
				return e.getStatus();
			}

		}

	}

	private static final class DisconnectFromBookmarksStoreJob extends Job {
		private final IRemoteBookmarksStore store;

		public DisconnectFromBookmarksStoreJob(IRemoteBookmarksStore store) {
			super("Disconnecting from " + store.getDescriptor().getLabel());
			this.store = store;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				store.disconnect(monitor);
				return Status.OK_STATUS;
			} catch (IOException e) {
				return new Status(IStatus.ERROR, BookmarksPlugin.PLUGIN_ID, 0,
						"Could not disconnect from " + store.getDescriptor().getLabel(), e);
			}
		}

	}

}
