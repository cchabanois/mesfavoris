package mesfavoris.internal.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import mesfavoris.BookmarksException;
import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.service.operations.ImportTeamProjectOperation;
import mesfavoris.model.Bookmark;

public class ImportTeamProjectFromBookmarkJob extends Job {
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