package org.chabanois.mesfavoris.bookmarktype;

import org.chabanois.mesfavoris.BookmarksException;
import org.chabanois.mesfavoris.model.Bookmark;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;

public interface IImportTeamProject {

	public abstract void importProject(Bookmark bookmark,
			IProgressMonitor monitor) throws BookmarksException;

	public abstract boolean canHandle(Bookmark bookmark);

	public abstract ImageDescriptor getIcon();
	
}