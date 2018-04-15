package mesfavoris.gdrive.handlers;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressService;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import mesfavoris.BookmarksException;
import mesfavoris.MesFavoris;
import mesfavoris.gdrive.Activator;
import mesfavoris.gdrive.StatusHelper;
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
		this.gDriveConnectionManager = Activator.getDefault().getGDriveConnectionManager();
		this.bookmarkMappingsStore = Activator.getDefault().getBookmarkMappingsStore();
		this.bookmarksService = MesFavoris.getBookmarksService();
		this.bookmarkMappings = Activator.getDefault().getBookmarkMappingsStore();
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
		ImportBookmarksFileDialog dialog = new ImportBookmarksFileDialog(shell, drive,
				gDriveConnectionManager.getApplicationFolderId(), bookmarkMappings);
		if (dialog.open() == Dialog.CANCEL) {
			return null;
		}
		List<File> files = dialog.getSelectedFiles();

		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		try {
			progressService.busyCursorWhile(monitor -> {
				SubMonitor subMonitor = SubMonitor.convert(monitor, "Importing bookmarks files", files.size());
				ImportBookmarkFileOperation importBookmarkFileOperation = new ImportBookmarkFileOperation(drive,
						bookmarkMappingsStore, bookmarksService,
						Optional.of(gDriveConnectionManager.getApplicationFolderId()));
				try {
					for (File file : files) {
						importBookmarkFileOperation.importBookmarkFile(bookmarkFolder.getId(), file.getId(),
								subMonitor.newChild(1));
					}
				} catch (BookmarksException | IOException e) {
					throw new InvocationTargetException(e);
				}

			});
		} catch (InvocationTargetException e) {
			StatusHelper.showError("Could not import bookmarks files", e.getCause(), false);
		} catch (InterruptedException e) {
			throw new ExecutionException("Could not import bookmarks files : cancelled");
		}
		return null;
	}

	@Override
	public boolean isEnabled() {
		Drive drive = gDriveConnectionManager.getDrive();
		if (drive == null) {
			return false;
		}
		return true;
	}

}
