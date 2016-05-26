package mesfavoris.internal.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import mesfavoris.BookmarksException;
import mesfavoris.BookmarksPlugin;
import mesfavoris.internal.operations.AddBookmarkFolderOperation;
import mesfavoris.internal.operations.RenameBookmarkOperation;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.remote.IRemoteBookmarksStore;
import mesfavoris.remote.RemoteBookmarkFolder;
import mesfavoris.remote.RemoteBookmarksStoreManager;
import mesfavoris.remote.IRemoteBookmarksStore.State;
import mesfavoris.validation.BookmarkModificationValidator;
import mesfavoris.validation.IBookmarkModificationValidator;

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
