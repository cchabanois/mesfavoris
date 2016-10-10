package mesfavoris.internal.service.operations;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import mesfavoris.BookmarksException;
import mesfavoris.bookmarktype.IBookmarkLocation;
import mesfavoris.bookmarktype.IBookmarkLocationProvider;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;

public class GotoBookmarkOperation {
	private final BookmarkDatabase bookmarkDatabase;
	private final IBookmarkLocationProvider bookmarkLocationProvider;
	private final IGotoBookmark gotoBookmark;

	public GotoBookmarkOperation(BookmarkDatabase bookmarkDatabase, IBookmarkLocationProvider bookmarkLocationProvider, IGotoBookmark gotoBookmark) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.bookmarkLocationProvider = bookmarkLocationProvider;
		this.gotoBookmark = gotoBookmark;
	}

	public void gotoBookmark(BookmarkId bookmarkId, IProgressMonitor monitor) throws BookmarksException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Goto bookmark", 100);
		Bookmark bookmark = bookmarkDatabase.getBookmarksTree().getBookmark(bookmarkId);
		IBookmarkLocation bookmarkLocation = bookmarkLocationProvider.getBookmarkLocation(bookmark, subMonitor.newChild(100));
		if (bookmarkLocation == null) {
			throw new BookmarksException("Could not find bookmark");
		}
		Display.getDefault().asyncExec(() -> {
			IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			gotoBookmark.gotoBookmark(workbenchWindow, bookmark, bookmarkLocation);
		});
	}

}
