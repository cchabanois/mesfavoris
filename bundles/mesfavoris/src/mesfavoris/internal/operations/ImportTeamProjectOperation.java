package mesfavoris.internal.operations;

import java.util.Optional;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import mesfavoris.BookmarksException;
import mesfavoris.bookmarktype.IImportTeamProject;
import mesfavoris.internal.bookmarktypes.ImportTeamProjectProvider;
import mesfavoris.model.Bookmark;

public class ImportTeamProjectOperation {
	private final ImportTeamProjectProvider importTeamProjectProvider;

	public ImportTeamProjectOperation(ImportTeamProjectProvider importTeamProjectProvider) {
		this.importTeamProjectProvider = importTeamProjectProvider;
	}

	public void importTeamProject(final Bookmark bookmark, final IProgressMonitor monitor) throws BookmarksException {
		Optional<IImportTeamProject> importTeamProject = importTeamProjectProvider.getHandler(bookmark);
		if (!importTeamProject.isPresent()) {
			throw new BookmarksException("Cannot import a team project from this bookmark");
		}
		importTeamProject(importTeamProject.get(), bookmark, monitor);
	}

	private void importTeamProject(final IImportTeamProject importTeamProject, final Bookmark bookmark,
			IProgressMonitor monitor) throws BookmarksException {
		try {
			ResourcesPlugin.getWorkspace().run(importMonitor -> {
				importTeamProject.importProject(bookmark, importMonitor);
			} , ResourcesPlugin.getWorkspace().getRoot(), 0 /* allow updates */, monitor);
		} catch (CoreException e) {
			throw new BookmarksException("Could not import team project from bookmark", e);
		}
	}

}
