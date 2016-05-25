package org.chabanois.mesfavoris.internal.jobs;

import org.chabanois.mesfavoris.BookmarksException;
import org.chabanois.mesfavoris.BookmarksPlugin;
import org.chabanois.mesfavoris.internal.operations.ImportTeamProjectOperation;
import org.chabanois.mesfavoris.model.Bookmark;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class ImportTeamProjectFromBookmarkJob extends Job {
	private final Bookmark bookmark;

	public ImportTeamProjectFromBookmarkJob(Bookmark bookmark) {
		super("Import team project from bookmark");
		this.bookmark = bookmark;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		ImportTeamProjectOperation importTeamProjectOperation = new ImportTeamProjectOperation(
				BookmarksPlugin.getImportTeamProjectProvider());
		try {
			importTeamProjectOperation.importTeamProject(bookmark, monitor);
		} catch (BookmarksException e) {
			return e.getStatus();
		}
		return Status.OK_STATUS;
	}

}