package mesfavoris.internal.service.operations;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;

import mesfavoris.BookmarksException;
import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.internal.service.operations.utils.INewBookmarkPositionProvider;
import mesfavoris.internal.service.operations.utils.NewBookmarkPosition;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;

public class AddBookmarkOperation {
	private final BookmarkDatabase bookmarkDatabase;
	private final INewBookmarkPositionProvider newBookmarkPositionProvider;
	private final IBookmarkPropertiesProvider bookmarkPropertiesProvider;

	public AddBookmarkOperation(BookmarkDatabase bookmarkDatabase,
			IBookmarkPropertiesProvider bookmarkPropertiesProvider,
			INewBookmarkPositionProvider newBookmarkPositionProvider) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.newBookmarkPositionProvider = newBookmarkPositionProvider;
		this.bookmarkPropertiesProvider = bookmarkPropertiesProvider;
	}

	public BookmarkId addBookmark(IWorkbenchPart part, ISelection selection, IProgressMonitor monitor)
			throws BookmarksException {
		Map<String, String> bookmarkProperties = new HashMap<String, String>();
		bookmarkPropertiesProvider.addBookmarkProperties(bookmarkProperties, part, selection, monitor);
		if (bookmarkProperties.isEmpty()) {
			throw new BookmarksException("Could not create bookmark from current selection");
		}
		bookmarkProperties.put(Bookmark.PROPERTY_CREATED, Instant.now().toString());
		BookmarkId bookmarkId = new BookmarkId();
		Bookmark bookmark = new Bookmark(bookmarkId, bookmarkProperties);
		addBookmark(part.getSite().getPage(), bookmark);
		return bookmarkId;
	}

	private void addBookmark(final IWorkbenchPage page, final Bookmark bookmark) throws BookmarksException {
		NewBookmarkPosition newBookmarkPosition = newBookmarkPositionProvider.getNewBookmarkPosition(page);
		bookmarkDatabase.modify(bookmarksTreeModifier -> {
			if (newBookmarkPosition.getBookmarkId().isPresent()) {
				bookmarksTreeModifier.addBookmarksAfter(newBookmarkPosition.getParentBookmarkId(),
						newBookmarkPosition.getBookmarkId().get(), Arrays.asList(bookmark));
			} else {
				bookmarksTreeModifier.addBookmarks(newBookmarkPosition.getParentBookmarkId(), Arrays.asList(bookmark));
			}
		});
	}

}
