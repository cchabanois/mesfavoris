package mesfavoris.internal.service.operations;

import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;

import mesfavoris.BookmarksException;
import mesfavoris.bookmarktype.IBookmarkLocationProvider;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.internal.numberedbookmarks.BookmarkNumber;
import mesfavoris.internal.numberedbookmarks.NumberedBookmarks;
import mesfavoris.model.BookmarkDatabase;
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

	public void gotoNumberedBookmark(BookmarkNumber bookmarkNumber, IProgressMonitor monitor) throws BookmarksException {
		Optional<BookmarkId> bookmarkId = numberedBookmarks.getBookmark(bookmarkNumber);
		if (!bookmarkId.isPresent()) {
			throw new BookmarksException("Bookmark does not exist anymore");
		}
		GotoBookmarkOperation operation = new GotoBookmarkOperation(bookmarkDatabase, bookmarkLocationProvider,
				gotoBookmark);
		operation.gotoBookmark(bookmarkId.get(), monitor);
	}

}
