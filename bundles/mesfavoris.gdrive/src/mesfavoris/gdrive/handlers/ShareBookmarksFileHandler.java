package mesfavoris.gdrive.handlers;

import java.io.IOException;
import java.util.Optional;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.google.api.services.drive.Drive;

import mesfavoris.gdrive.Activator;
import mesfavoris.gdrive.connection.GDriveConnectionManager;
import mesfavoris.gdrive.dialogs.ShareBookmarksFileDialog;
import mesfavoris.gdrive.mappings.BookmarkMappingsStore;
import mesfavoris.gdrive.operations.ShareFileOperation;
import mesfavoris.handlers.AbstractBookmarkHandler;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkFolder;

public class ShareBookmarksFileHandler extends AbstractBookmarkHandler {
	private final GDriveConnectionManager gDriveConnectionManager;
	private final BookmarkMappingsStore bookmarkMappingsStore;

	public ShareBookmarksFileHandler() {
		this.gDriveConnectionManager = Activator.getGDriveConnectionManager();
		this.bookmarkMappingsStore = Activator.getBookmarkMappingsStore();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		BookmarkFolder bookmarkFolder = getSelectedBookmarkFolder(selection);
		if (bookmarkFolder == null) {
			return null;
		}
		Optional<String> fileId = bookmarkMappingsStore.getMapping(bookmarkFolder.getId()).map(mapping->mapping.getFileId());
		if (!fileId.isPresent()) {
			return null;
		}
		Shell shell = HandlerUtil.getActiveShell(event);
		Drive drive = gDriveConnectionManager.getDrive();
		if (drive == null) {
			return null;
		}
		ShareBookmarksFileDialog dialog = new ShareBookmarksFileDialog(shell);
		if (dialog.open() == Dialog.CANCEL) {
			return null;
		}
		ShareFileOperation shareFileOperation = new ShareFileOperation(drive);
		try {
			shareFileOperation.shareWithUser(fileId.get(), dialog.getEmail(), dialog.canWrite());
		} catch (IOException e) {
			throw new ExecutionException("Could not share bookmarks file with user", e);
		}
		return null;
	}

	@Override
	public boolean isEnabled() {
		BookmarkFolder bookmarkFolder = getSelectedBookmarkFolder(getSelection());
		if (bookmarkFolder == null) {
			return false;
		}
		Optional<String> fileId = bookmarkMappingsStore.getMapping(bookmarkFolder.getId()).map(mapping->mapping.getFileId());
		if (!fileId.isPresent()) {
			return false;
		}
		Drive drive = gDriveConnectionManager.getDrive();
		if (drive == null) {
			return false;
		}
		return true;
	}

	private BookmarkFolder getSelectedBookmarkFolder(IStructuredSelection selection) {
		if (selection.isEmpty()) {
			return null;
		}
		Bookmark bookmark = (Bookmark) selection.getFirstElement();
		if (!(bookmark instanceof BookmarkFolder)) {
			return null;
		}
		BookmarkFolder bookmarkFolder = (BookmarkFolder) bookmark;
		return bookmarkFolder;
	}

}
