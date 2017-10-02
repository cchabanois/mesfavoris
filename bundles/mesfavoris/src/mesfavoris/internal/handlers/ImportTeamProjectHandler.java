package mesfavoris.internal.handlers;

import java.util.Map;
import java.util.Optional;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.progress.IProgressService;

import mesfavoris.BookmarksException;
import mesfavoris.bookmarktype.IImportTeamProject;
import mesfavoris.handlers.AbstractBookmarkHandler;
import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.bookmarktypes.ImportTeamProjectProvider;
import mesfavoris.internal.service.operations.ImportTeamProjectOperation;
import mesfavoris.model.Bookmark;

public class ImportTeamProjectHandler extends AbstractBookmarkHandler implements IElementUpdater {
	private final ImportTeamProjectProvider importTeamProjectProvider;

	public ImportTeamProjectHandler() {
		this.importTeamProjectProvider = BookmarksPlugin.getDefault().getImportTeamProjectProvider();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		Bookmark bookmark = getSelectedBookmark(selection);
		ImportTeamProjectFromBookmarkJob job = new ImportTeamProjectFromBookmarkJob(bookmark);
		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		progressService.showInDialog(HandlerUtil.getActiveShell(event), job);
		job.schedule();
		return null;
	}
	
	@Override
	public boolean isEnabled() {
		Bookmark bookmark = getSelectedBookmark(getSelection());
		if (bookmark == null) {
			return false;
		}
		Optional<IImportTeamProject> importTeamProject = importTeamProjectProvider.getHandler(bookmark);
		return importTeamProject.isPresent();
	}

	private Bookmark getSelectedBookmark(IStructuredSelection selection) {
		if (selection.isEmpty()) {
			return null;
		}
		if (!(selection.getFirstElement() instanceof Bookmark)) {
			return null;
		}
		Bookmark bookmark = (Bookmark) selection.getFirstElement();
		return bookmark;
	}

	@Override
	public void updateElement(UIElement element, Map parameters) {
		Bookmark bookmark = getSelectedBookmark(getSelection());
		if (bookmark == null) {
			return;
		}
		Optional<IImportTeamProject> importTeamProject = importTeamProjectProvider.getHandler(bookmark);
		if (!importTeamProject.isPresent()) {
			return;
		}
		element.setIcon(importTeamProject.get().getIcon());
	}

	private static class ImportTeamProjectFromBookmarkJob extends Job {
		private final Bookmark bookmark;

		public ImportTeamProjectFromBookmarkJob(Bookmark bookmark) {
			super("Import team project from bookmark");
			this.bookmark = bookmark;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			ImportTeamProjectOperation importTeamProjectOperation = new ImportTeamProjectOperation(
					BookmarksPlugin.getDefault().getImportTeamProjectProvider());
			try {
				importTeamProjectOperation.importTeamProject(bookmark, monitor);
			} catch (BookmarksException e) {
				return e.getStatus();
			}
			return Status.OK_STATUS;
		}

	}	
	
}
