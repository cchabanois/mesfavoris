package org.chabanois.mesfavoris.internal.actions;

import org.chabanois.mesfavoris.BookmarksException;
import org.chabanois.mesfavoris.BookmarksPlugin;
import org.chabanois.mesfavoris.internal.operations.AddToRemoteBookmarksStoreOperation;
import org.chabanois.mesfavoris.model.BookmarkFolder;
import org.chabanois.mesfavoris.remote.AbstractRemoteBookmarksStore;
import org.chabanois.mesfavoris.remote.IRemoteBookmarksStore;
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

public class AddToRemoteBookmarksStoreAction extends SelectionProviderAction implements IWorkbenchAction {
	private final IEventBroker eventBroker;
	private final IRemoteBookmarksStore store;
	private final EventHandler connectionEventHandler;
	private final AddToRemoteBookmarksStoreOperation operation;

	public AddToRemoteBookmarksStoreAction(IEventBroker eventBroker, ISelectionProvider provider,
			IRemoteBookmarksStore store) {
		super(provider, "Add to " + store.getDescriptor().getLabel());
		this.eventBroker = eventBroker;
		this.store = store;
		setImageDescriptor(store.getDescriptor().getImageDescriptor());
		connectionEventHandler = event -> updateEnablement();
		eventBroker.subscribe(AbstractRemoteBookmarksStore.getConnectedTopic(store.getDescriptor().getId()), connectionEventHandler);
		operation = new AddToRemoteBookmarksStoreOperation(
				BookmarksPlugin.getBookmarkDatabase(),
				BookmarksPlugin.getRemoteBookmarksStoreManager());
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
		AddToRemoteBookmarksStoreJob job = new AddToRemoteBookmarksStoreJob(bookmarkFolder);
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
		setEnabled(operation.canAddToRemoteBookmarkStore(store.getDescriptor().getId(), bookmarkFolder.getId()));
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

	private final class AddToRemoteBookmarksStoreJob extends Job {
		private final BookmarkFolder bookmarkFolder;

		public AddToRemoteBookmarksStoreJob(BookmarkFolder bookmarkFolder) {
			super("Adding bookmark folder to " + store.getDescriptor().getLabel());
			this.bookmarkFolder = bookmarkFolder;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				operation.addToRemoteBookmarksStore(store.getDescriptor().getId(), bookmarkFolder.getId(), monitor);
				return Status.OK_STATUS;
			} catch (BookmarksException e) {
				return e.getStatus();
			}

		}

	}

}
