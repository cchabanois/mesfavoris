package mesfavoris.internal.handlers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressService;

import mesfavoris.BookmarksException;
import mesfavoris.MesFavoris;
import mesfavoris.handlers.AbstractBookmarkHandler;
import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.StatusHelper;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.remote.IRemoteBookmarksStore;
import mesfavoris.remote.IRemoteBookmarksStore.State;
import mesfavoris.remote.RemoteBookmarkFolder;
import mesfavoris.remote.RemoteBookmarksStoreManager;
import mesfavoris.viewers.BookmarksTableLabelProvider;

public class DeleteSharedBookmarkFolderHandler extends AbstractBookmarkHandler {
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;

	public DeleteSharedBookmarkFolderHandler() {
		this.remoteBookmarksStoreManager = BookmarksPlugin.getDefault().getRemoteBookmarksStoreManager();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		BookmarkFolder bookmarkFolder = (BookmarkFolder) selection.getFirstElement();
		if (bookmarkFolder == null) {
			return null;
		}
		RemoteBookmarkFolder remoteBookmarkFolder = remoteBookmarksStoreManager
				.getRemoteBookmarkFolder(bookmarkFolder.getId());
		if (remoteBookmarkFolder == null) {
			return null;
		}
		ConfirmDeleteDialog dialog = new ConfirmDeleteDialog(HandlerUtil.getActiveShell(event), remoteBookmarkFolder);
		if (dialog.open() != Window.OK) {
			return null;
		}

		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		try {
			progressService.busyCursorWhile(monitor -> {
				try {
					if (dialog.isDeleteFromRemoteStore()) {
						bookmarksService.removeFromRemoteBookmarksStore(remoteBookmarkFolder.getRemoteBookmarkStoreId(),
								remoteBookmarkFolder.getBookmarkFolderId(), monitor);
					}
					bookmarksService.deleteBookmarks(getAsBookmarkIds(selection), true);
				} catch (BookmarksException e) {
					throw new InvocationTargetException(e);
				}

			});
		} catch (InvocationTargetException e) {
			StatusHelper.showError("Could not remove shared bookmark folder", e.getCause(), false);
		} catch (InterruptedException e) {
			throw new ExecutionException("Could not remove shared bookmark folder : cancelled");
		}		

		return null;
	}

	private static class ConfirmDeleteDialog extends MessageDialog {
		private final RemoteBookmarkFolder remoteBookmarkFolder;
		private final BookmarkDatabase bookmarkDatabase;
		private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;
		private Button deleteFromRemoteStoreButton;
		private boolean deleteFromRemoteStore;

		public ConfirmDeleteDialog(Shell parentShell, RemoteBookmarkFolder remoteBookmarkFolder) {
			super(parentShell, "Remove shared bookmark folder", null,
					"Are you sure you want to remove this shared bookmark folder ?", MessageDialog.CONFIRM,
					new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, 0);
			setShellStyle(getShellStyle() | SWT.RESIZE | SWT.SHEET);
			this.bookmarkDatabase = MesFavoris.getBookmarkDatabase();
			this.remoteBookmarksStoreManager = BookmarksPlugin.getDefault().getRemoteBookmarksStoreManager();
			this.remoteBookmarkFolder = remoteBookmarkFolder;
		}

		@Override
		protected Control createCustomArea(Composite parent) {
			Composite area = new Composite(parent, SWT.NONE);
			area.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
			area.setLayout(new GridLayout());

			TableViewer tableViewer = new TableViewer(area);
			GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
			tableViewer.getTable().setLayoutData(gridData);
			tableViewer.setContentProvider(new ArrayContentProvider());
			BookmarksTableLabelProvider labelProvider = new BookmarksTableLabelProvider(
					MesFavoris.getBookmarkDatabase(), BookmarksPlugin.getDefault().getRemoteBookmarksStoreManager(),
					BookmarksPlugin.getDefault().getBookmarkLabelProvider());
			tableViewer.setLabelProvider(labelProvider);
			BookmarkFolder bookmarkFolder = (BookmarkFolder) bookmarkDatabase.getBookmarksTree()
					.getBookmark(remoteBookmarkFolder.getBookmarkFolderId());
			tableViewer.setInput(new BookmarkFolder[] { bookmarkFolder });

			IRemoteBookmarksStore remoteBookmarkStore = remoteBookmarksStoreManager
					.getRemoteBookmarksStore(remoteBookmarkFolder.getRemoteBookmarkStoreId());
			deleteFromRemoteStoreButton = new Button(area, SWT.CHECK);
			remoteBookmarkStore.getDescriptor().getLabel();
			deleteFromRemoteStoreButton.setText("Delete from " + remoteBookmarkStore.getDescriptor().getLabel());
			deleteFromRemoteStoreButton
					.setEnabled(remoteBookmarkStore.getState() == State.connected && !isReadOnly(remoteBookmarkFolder));
			return area;
		}

		private boolean isReadOnly(RemoteBookmarkFolder remoteBookmarkFolder) {
			return Boolean.TRUE.toString()
					.equalsIgnoreCase(remoteBookmarkFolder.getProperties().get(RemoteBookmarkFolder.PROP_READONLY));
		}

		@Override
		protected void buttonPressed(int buttonId) {
			deleteFromRemoteStore = deleteFromRemoteStoreButton.getSelection();
			super.buttonPressed(buttonId);
		}

		public boolean isDeleteFromRemoteStore() {
			return deleteFromRemoteStore;
		}

	}
}
