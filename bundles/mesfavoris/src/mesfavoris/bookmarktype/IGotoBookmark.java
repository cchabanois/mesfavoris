package mesfavoris.bookmarktype;

import org.eclipse.ui.IWorkbenchWindow;

import mesfavoris.model.Bookmark;

public interface IGotoBookmark {

	public boolean gotoBookmark(IWorkbenchWindow window, Bookmark bookmark, IBookmarkLocation bookmarkLocation);

}
