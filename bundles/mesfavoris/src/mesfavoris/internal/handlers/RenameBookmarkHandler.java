package mesfavoris.internal.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import mesfavoris.BookmarksException;
import mesfavoris.handlers.AbstractBookmarkHandler;
import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.model.Bookmark;
import mesfavoris.remote.IRemoteBookmarksStore.State;
import mesfavoris.remote.RemoteBookmarksStoreManager;
import mesfavoris.service.IBookmarksService;

public class RenameBookmarkHandler extends AbstractBookmarkHandler {
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;

	public RenameBookmarkHandler() {
		this.remoteBookmarksStoreManager = BookmarksPlugin.getDefault().getRemoteBookmarksStoreManager();
	}

	public RenameBookmarkHandler(IBookmarksService bookmarksService,
			RemoteBookmarksStoreManager remoteBookmarksStoreManager) {
		super(bookmarksService);
		this.remoteBookmarksStoreManager = remoteBookmarksStoreManager;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		Bookmark bookmark = getSelectedBookmark(selection);
		Shell shell = HandlerUtil.getActiveShell(event);
		String newName = askBookmarkName(shell, bookmark.getPropertyValue(Bookmark.PROPERTY_NAME));
		if (newName == null) {
			return null;
		}
		try {
			bookmarksService.renameBookmark(bookmark.getId(), newName);
		} catch (BookmarksException e) {
			throw new ExecutionException("Could not rename bookmark", e);
		}

		return null;

	}

	private String askBookmarkName(Shell parentShell, String currentName) {
		InputDialog inputDialog = new InputDialog(parentShell, "Rename bookmark", "Enter name", currentName, null);
		if (inputDialog.open() == Window.OK) {
			return inputDialog.getValue();
		}
		return null;
	}

	private Bookmark getSelectedBookmark(IStructuredSelection selection) {
		if (selection.isEmpty()) {
			return null;
		}
		Bookmark bookmark = (Bookmark) selection.getFirstElement();
		return bookmark;
	}

	@Override
	public boolean isEnabled() {
		Bookmark bookmark = getSelectedBookmark(getSelection());
		if (bookmark == null) {
			return false;
		}

		return remoteBookmarksStoreManager
				.getRemoteBookmarkFolderContaining(bookmarksService.getBookmarksTree(), bookmark.getId())
				.flatMap(remoteBookmarkFolder -> remoteBookmarksStoreManager
						.getRemoteBookmarksStore(remoteBookmarkFolder.getRemoteBookmarkStoreId()))
				.map(store -> store.getState() == State.connected).orElse(true);
	}

}
