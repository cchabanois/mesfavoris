package org.chabanois.mesfavoris.internal.operations;

import java.util.Optional;

import org.chabanois.mesfavoris.BookmarksException;
import org.chabanois.mesfavoris.bookmarktype.IImportTeamProject;
import org.chabanois.mesfavoris.internal.bookmarktypes.ImportTeamProjectProvider;
import org.chabanois.mesfavoris.model.Bookmark;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

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
