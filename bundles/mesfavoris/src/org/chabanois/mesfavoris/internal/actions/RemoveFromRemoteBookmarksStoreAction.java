package org.chabanois.mesfavoris.internal.actions;

import org.chabanois.mesfavoris.BookmarksException;
import org.chabanois.mesfavoris.BookmarksPlugin;
import org.chabanois.mesfavoris.internal.operations.RemoveFromRemoteBookmarksStoreOperation;
import org.chabanois.mesfavoris.model.BookmarkFolder;
import org.chabanois.mesfavoris.remote.AbstractRemoteBookmarksStore;
import org.chabanois.mesfavoris.remote.IRemoteBookmarksStore;
import org.chabanois.mesfavoris.remote.IRemoteBookmarksStore.State;
import org.chabanois.mesfavoris.remote.IRemoteBookmarksStoreDescriptor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.osgi.service.event.EventHandler;

public class RemoveFromRemoteBookmarksStoreAction extends SelectionProviderAction implements IWorkbenchAction {
	private final IEventBroker eventBroker;
	private final IRemoteBookmarksStore remoteBookmarksStore;
	private final EventHandler connectionEventHandler;

	public RemoveFromRemoteBookmarksStoreAction(IEventBroker eventBroker, ISelectionProvider provider,
			IRemoteBookmarksStore store) {
		super(provider, "Remove from " + store.getDescriptor().getLabel());
		this.eventBroker = eventBroker;
		this.remoteBookmarksStore = store;
		setImageDescriptor(store.getDescriptor().getImageDescriptor());
		connectionEventHandler = event -> updateEnablement();
		eventBroker.subscribe(AbstractRemoteBookmarksStore.getConnectedTopic(store.getDescriptor().getId()),
				connectionEventHandler);
		updateEnablement();
	}

	@Override
	public void dispose() {
		super.dispose();
		eventBroker.unsubscribe(connectionEventHandler);
	}

	@Override
	public void run() {
		BookmarkFolder bookmarkFolder = getSelectedBookmarkFolder();
		RemoveFromRemoteBookmarksStoreJob job = new RemoveFromRemoteBookmarksStoreJob(
				remoteBookmarksStore.getDescriptor(), bookmarkFolder);
		job.setUser(true);
		job.schedule();
	}

	@Override
	public void selectionChanged(IStructuredSelection selection) {
		updateEnablement();
	}

	private void updateEnablement() {
		BookmarkFolder bookmarkFolder = getSelectedBookmarkFolder();
		if (bookmarkFolder == null) {
			setEnabled(false);
			return;
		}
		if (remoteBookmarksStore.getState() != State.connected) {
			setEnabled(false);
			return;
		}
		if (!remoteBookmarksStore.getRemoteBookmarkFolderIds().contains(bookmarkFolder.getId())) {
			setEnabled(false);
			return;
		}
		setEnabled(true);
	}

	private BookmarkFolder getSelectedBookmarkFolder() {
		IStructuredSelection selection = (IStructuredSelection) getSelection();
		if (selection.size() != 1) {
			return null;
		}
		if (!(selection.getFirstElement() instanceof BookmarkFolder)) {
			return null;
		}
		return (BookmarkFolder) selection.getFirstElement();
	}

	private static final class RemoveFromRemoteBookmarksStoreJob extends Job {
		private final IRemoteBookmarksStoreDescriptor storeDescriptor;
		private final BookmarkFolder bookmarkFolder;

		public RemoveFromRemoteBookmarksStoreJob(IRemoteBookmarksStoreDescriptor storeDescriptor,
				BookmarkFolder bookmarkFolder) {
			super("Removing bookmark folder from " + storeDescriptor.getLabel());
			this.storeDescriptor = storeDescriptor;
			this.bookmarkFolder = bookmarkFolder;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				RemoveFromRemoteBookmarksStoreOperation operation = new RemoveFromRemoteBookmarksStoreOperation(
						BookmarksPlugin.getBookmarkDatabase(), BookmarksPlugin.getRemoteBookmarksStoreManager());
				operation.removeFromRemoteBookmarksStore(storeDescriptor.getId(), bookmarkFolder.getId(), monitor);
				return Status.OK_STATUS;
			} catch (BookmarksException e) {
				return e.getStatus();
			}

		}

	}

}
