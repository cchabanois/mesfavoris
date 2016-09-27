package mesfavoris.gdrive.handlers;

import java.io.IOException;
import java.util.Optional;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import mesfavoris.BookmarksException;
import mesfavoris.BookmarksPlugin;
import mesfavoris.gdrive.Activator;
import mesfavoris.gdrive.connection.GDriveConnectionManager;
import mesfavoris.gdrive.dialogs.ImportBookmarksFileDialog;
import mesfavoris.gdrive.mappings.BookmarkMappingsStore;
import mesfavoris.gdrive.mappings.IBookmarkMappings;
import mesfavoris.gdrive.operations.ImportBookmarkFileOperation;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.service.IBookmarksService;

public class ImportBookmarksFromGdriveHandler extends AbstractHandler {
	private final GDriveConnectionManager gDriveConnectionManager;
	private final BookmarkMappingsStore bookmarkMappingsStore;
	private final IBookmarksService bookmarksService;
	private final IBookmarkMappings bookmarkMappings;

	public ImportBookmarksFromGdriveHandler() {
		this.gDriveConnectionManager = Activator.getGDriveConnectionManager();
		this.bookmarkMappingsStore = Activator.getBookmarkMappingsStore();
		this.bookmarksService = BookmarksPlugin.getBookmarksService();
		this.bookmarkMappings = Activator.getBookmarkMappingsStore();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		BookmarkFolder bookmarkFolder = selection.isEmpty() ? bookmarksService.getBookmarksTree().getRootFolder()
				: (BookmarkFolder) selection.getFirstElement();
		Shell shell = HandlerUtil.getActiveShell(event);
		Drive drive = gDriveConnectionManager.getDrive();
		if (drive == null) {
			return null;
		}
		ImportBookmarksFileDialog dialog = new ImportBookmarksFileDialog(shell, gDriveConnectionManager,
				bookmarkMappings);
		if (dialog.open() == Dialog.CANCEL) {
			return null;
		}
		File file = dialog.getFile();
		ImportBookmarkFileOperation importBookmarkFileOperation = new ImportBookmarkFileOperation(drive,
				bookmarkMappingsStore, bookmarksService, Optional.of(gDriveConnectionManager.getApplicationFolderId()));
		try {
			importBookmarkFileOperation.importBookmarkFile(bookmarkFolder.getId(), file.getId(),
					new NullProgressMonitor());
		} catch (BookmarksException | IOException e) {
			throw new ExecutionException("Could not import bookmarks file", e);
		}
		return null;
	}

}
