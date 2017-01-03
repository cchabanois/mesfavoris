package mesfavoris.path.internal;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import mesfavoris.bookmarktype.IBookmarkLocation;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.commons.ui.ide.ShowInSystemExplorer;
import mesfavoris.model.Bookmark;

public class GotoExternalFolderBookmark implements IGotoBookmark {

	@Override
	public boolean gotoBookmark(IWorkbenchWindow window, Bookmark bookmark, IBookmarkLocation bookmarkLocation) {
		if (!(bookmarkLocation instanceof ExternalFolderBookmarkLocation)) {
			return false;
		}
		ExternalFolderBookmarkLocation externalFolderBookmarkLocation = (ExternalFolderBookmarkLocation) bookmarkLocation;

		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		try {
			progressService.busyCursorWhile(monitor -> {
				ShowInSystemExplorer showInSystemExplorer = new ShowInSystemExplorer();
				try {
					showInSystemExplorer.showInSystemExplorer(externalFolderBookmarkLocation.getFileSystemPath().toFile(),
							monitor);
				} catch (IOException e) {
					throw new InvocationTargetException(e);
				}

			});
		} catch (InvocationTargetException e) {
			StatusHelper.showError("Could not goto external folder", e.getCause(), true);
		} catch (InterruptedException e) {
			// cancelled
		}
		return true;
	}

}
