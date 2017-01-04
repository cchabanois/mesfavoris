package mesfavoris.bookmarktype;

import org.eclipse.ui.IWorkbenchWindow;

import mesfavoris.model.Bookmark;

public interface IGotoBookmark {

	/**
	 * Go to the location corresponding to the given {@link Bookmark}
	 * 
	 * @param window
	 * @param bookmark
	 * @param bookmarkLocation
	 * @return true if it succeeds, false otherwise
	 */
	public boolean gotoBookmark(IWorkbenchWindow window, Bookmark bookmark, IBookmarkLocation bookmarkLocation);

}
