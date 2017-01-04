package mesfavoris.bookmarktype;

import org.eclipse.core.runtime.IProgressMonitor;

import mesfavoris.model.Bookmark;

public interface IBookmarkLocationProvider {

	/**
	 * Get the location corresponding to the given {@link Bookmark}
	 * 
	 * @param bookmark
	 * @param monitor
	 * @return the bookmark location or null if not found
	 */
	IBookmarkLocation getBookmarkLocation(Bookmark bookmark, IProgressMonitor monitor);

}
