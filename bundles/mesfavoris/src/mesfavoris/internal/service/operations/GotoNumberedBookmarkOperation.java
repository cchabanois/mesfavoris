package mesfavoris.internal.service.operations;

import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import mesfavoris.BookmarksException;
import mesfavoris.bookmarktype.IBookmarkLocationProvider;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.internal.numberedbookmarks.BookmarkNumber;
import mesfavoris.internal.numberedbookmarks.NumberedBookmarks;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;

public class GotoNumberedBookmarkOperation {
	private final NumberedBookmarks numberedBookmarks;
	private final BookmarkDatabase bookmarkDatabase;
	private final IBookmarkLocationProvider bookmarkLocationProvider;
	private final IGotoBookmark gotoBookmark;

	public GotoNumberedBookmarkOperation(NumberedBookmarks numberedBookmarks, BookmarkDatabase bookmarkDatabase,
			IBookmarkLocationProvider bookmarkLocationProvider, IGotoBookmark gotoBookmark) {
		this.numberedBookmarks = numberedBookmarks;
		this.bookmarkDatabase = bookmarkDatabase;
		this.bookmarkLocationProvider = bookmarkLocationProvider;
		this.gotoBookmark = gotoBookmark;
	}

	public void gotoNumberedBookmark(BookmarkNumber bookmarkNumber, IProgressMonitor monitor)
			throws BookmarksException {
		Optional<BookmarkId> bookmarkId = numberedBookmarks.getBookmark(bookmarkNumber);
		if (!bookmarkId.isPresent()) {
			throw new BookmarksException("Bookmark does not exist anymore");
		}
		if (bookmarkDatabase.getBookmarksTree().getBookmark(bookmarkId.get()) instanceof BookmarkFolder) {
			gotoBookmarkFolder(bookmarkId.get());
		} else {
			gotoBookmark(bookmarkId.get(), monitor);
		}
	}

	private void gotoBookmarkFolder(BookmarkId bookmarkId) {
		Display.getDefault().asyncExec(() -> {
			ShowInBookmarksViewOperation operation = new ShowInBookmarksViewOperation(bookmarkDatabase);
			IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (workbenchWindow == null) {
				return;
			}
			IWorkbenchPage page = workbenchWindow.getActivePage();
			if (page == null) {
				return;
			}
			operation.showInBookmarksView(page, bookmarkId, true);
		});
	}

	private void gotoBookmark(BookmarkId bookmarkId, IProgressMonitor monitor) throws BookmarksException {
		GotoBookmarkOperation operation = new GotoBookmarkOperation(bookmarkDatabase, bookmarkLocationProvider,
				gotoBookmark);
		operation.gotoBookmark(bookmarkId, monitor);
	}

}
