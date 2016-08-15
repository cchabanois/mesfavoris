package mesfavoris.bookmarktype;

import org.eclipse.core.runtime.IProgressMonitor;

import mesfavoris.model.Bookmark;

public interface IBookmarkLocationProvider {

	IBookmarkLocation getBookmarkLocation(Bookmark bookmark, IProgressMonitor monitor);	
	
}
