package org.chabanois.mesfavoris.internal.handlers;

import org.chabanois.mesfavoris.BookmarksException;
import org.chabanois.mesfavoris.BookmarksPlugin;
import org.chabanois.mesfavoris.internal.operations.AddBookmarkFolderOperation;
import org.chabanois.mesfavoris.internal.operations.RenameBookmarkOperation;
import org.chabanois.mesfavoris.model.Bookmark;
import org.chabanois.mesfavoris.model.BookmarkDatabase;
import org.chabanois.mesfavoris.model.BookmarkFolder;
import org.chabanois.mesfavoris.remote.IRemoteBookmarksStore;
import org.chabanois.mesfavoris.remote.RemoteBookmarkFolder;
import org.chabanois.mesfavoris.remote.RemoteBookmarksStoreManager;
import org.chabanois.mesfavoris.remote.IRemoteBookmarksStore.State;
import org.chabanois.mesfavoris.validation.BookmarkModificationValidator;
import org.chabanois.mesfavoris.validation.IBookmarkModificationValidator;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

public class RenameBookmarkHandler extends AbstractBookmarkHandler {
	private final BookmarkDatabase bookmarkDatabase;
	private final IBookmarkModificationValidator bookmarkModificationValidator;
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;

	public RenameBookmarkHandler() {
		this.bookmarkDatabase = BookmarksPlugin.getBookmarkDatabase();
		this.remoteBookmarksStoreManager = BookmarksPlugin.getRemoteBookmarksStoreManager();
		this.bookmarkModificationValidator = new BookmarkModificationValidator(
				BookmarksPlugin.getRemoteBookmarksStoreManager());
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
		RenameBookmarkOperation operation = new RenameBookmarkOperation(bookmarkDatabase,
				bookmarkModificationValidator);
		try {
			operation.renameBookmark(bookmark.getId(), newName);
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
		RemoteBookmarkFolder remoteBookmarkFolder = remoteBookmarksStoreManager
				.getRemoteBookmarkFolderContaining(bookmarkDatabase.getBookmarksTree(), bookmark.getId());
		if (remoteBookmarkFolder == null) {
			return true;
		}
		IRemoteBookmarksStore remoteBookmarksStore = remoteBookmarksStoreManager
				.getRemoteBookmarksStore(remoteBookmarkFolder.getRemoteBookmarkStoreId());
		return remoteBookmarksStore.getState() == State.connected;
	}	
	
}
