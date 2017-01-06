package mesfavoris.path.internal;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.resources.ProjectExplorer;

import mesfavoris.bookmarktype.IBookmarkLocation;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.model.Bookmark;

public class GotoWorkspaceFolderBookmark implements IGotoBookmark {

	@Override
	public boolean gotoBookmark(IWorkbenchWindow window, Bookmark bookmark, IBookmarkLocation bookmarkLocation) {
		if (!(bookmarkLocation instanceof WorkspaceFolderBookmarkLocation)) {
			return false;
		}
		WorkspaceFolderBookmarkLocation workspaceFileBookmarkLocation = (WorkspaceFolderBookmarkLocation) bookmarkLocation;
		IFolder folder = workspaceFileBookmarkLocation.getWorkspaceFolder();
		try {
			ProjectExplorer projectExplorer = (ProjectExplorer) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().showView(ProjectExplorer.VIEW_ID);
			projectExplorer.selectReveal(new StructuredSelection(folder));
			return true;
		} catch (PartInitException e) {
			return false;
		}
	}

}
