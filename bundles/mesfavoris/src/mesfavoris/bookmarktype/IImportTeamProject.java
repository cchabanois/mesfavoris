package mesfavoris.bookmarktype;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;

import mesfavoris.BookmarksException;
import mesfavoris.model.Bookmark;

public interface IImportTeamProject {

	public abstract void importProject(Bookmark bookmark,
			IProgressMonitor monitor) throws BookmarksException;

	public abstract boolean canHandle(Bookmark bookmark);

	public abstract ImageDescriptor getIcon();
	
}