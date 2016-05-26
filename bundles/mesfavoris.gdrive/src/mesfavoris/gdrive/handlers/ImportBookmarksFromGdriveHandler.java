package mesfavoris.gdrive.handlers;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import mesfavoris.BookmarksException;
import mesfavoris.BookmarksPlugin;
import mesfavoris.gdrive.Activator;
import mesfavoris.gdrive.connection.GDriveConnectionManager;
import mesfavoris.gdrive.mappings.BookmarkMappingsStore;
import mesfavoris.gdrive.operations.GetBookmarkFilesOperation;
import mesfavoris.gdrive.operations.ImportBookmarkFileOperation;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.service.IBookmarksService;

public class ImportBookmarksFromGdriveHandler extends AbstractHandler {
	private final GDriveConnectionManager gDriveConnectionManager;
	private final BookmarkMappingsStore bookmarkMappingsStore;
	private final IBookmarksService bookmarksService;

	public ImportBookmarksFromGdriveHandler() {
		this.gDriveConnectionManager = Activator.getGDriveConnectionManager();
		this.bookmarkMappingsStore = Activator.getBookmarkMappingsStore();
		this.bookmarksService = BookmarksPlugin.getBookmarksService();
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
		List<File> bookmarkFiles = getBookmarkFiles(drive);
		File file = selectBookmarkFile(shell, bookmarkFiles);
		if (file == null) {
			return null;
		}
		ImportBookmarkFileOperation importBookmarkFileOperation = new ImportBookmarkFileOperation(drive,
				bookmarkMappingsStore, bookmarksService);
		try {
			importBookmarkFileOperation.importBookmarkFile(bookmarkFolder.getId(), file.getId(),
					new NullProgressMonitor());
		} catch (BookmarksException | IOException e) {
			throw new ExecutionException("Could not import bookmarks file", e);
		}
		return null;
	}

	private List<File> getBookmarkFiles(Drive drive) throws ExecutionException {
		GetBookmarkFilesOperation operation = new GetBookmarkFilesOperation(drive);
		List<File> bookmarkFiles;
		try {
			bookmarkFiles = operation.getBookmarkFiles();
		} catch (IOException e) {
			throw new ExecutionException("Could not retrieve bookmark files", e);
		}
		return bookmarkFiles;
	}

	private File selectBookmarkFile(Shell shell, List<File> bookmarkFiles) {
		ListDialog dialog = new ListDialog(shell);
		dialog.setTitle("Import GDrive bookmarks");
		dialog.setAddCancelButton(true);
		dialog.setLabelProvider(new GdriveFileLabelProvider());
		dialog.setMessage("Select a bookmarks file to import");
		dialog.setContentProvider(new ArrayContentProvider());
		dialog.setInput(bookmarkFiles);
		if (dialog.open() == Window.OK) {
			return (File) dialog.getResult()[0];
		} else {
			return null;
		}
	}

	private static class GdriveFileLabelProvider extends LabelProvider {

		@Override
		public String getText(Object element) {
			File file = (File) element;
			return file.getTitle();
		}

	}

}
