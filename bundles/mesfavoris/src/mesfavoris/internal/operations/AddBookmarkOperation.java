package mesfavoris.internal.operations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

import mesfavoris.BookmarksException;
import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.workspace.DefaultBookmarkFolderManager;

public class AddBookmarkOperation {
	private final BookmarkDatabase bookmarkDatabase;
	private final DefaultBookmarkFolderManager defaultBookmarkFolderManager;
	private final IBookmarkPropertiesProvider bookmarkPropertiesProvider;

	public AddBookmarkOperation(BookmarkDatabase bookmarkDatabase,
			IBookmarkPropertiesProvider bookmarkPropertiesProvider,
			DefaultBookmarkFolderManager defaultBookmarkFolderManager) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.defaultBookmarkFolderManager = defaultBookmarkFolderManager;
		this.bookmarkPropertiesProvider = bookmarkPropertiesProvider;
	}

	public BookmarkId addBookmark(IWorkbenchPart part, ISelection selection, IProgressMonitor monitor)
			throws BookmarksException {
		Map<String, String> bookmarkProperties = new HashMap<String, String>();
		bookmarkPropertiesProvider.addBookmarkProperties(bookmarkProperties, part, selection, monitor);
		if (bookmarkProperties.isEmpty()) {
			throw new BookmarksException("Could not create bookmark from current selection");
		}
		BookmarkId bookmarkId = new BookmarkId();
		Bookmark bookmark = new Bookmark(bookmarkId, bookmarkProperties);
		addBookmark(bookmark);
		return bookmarkId;
	}

	private void addBookmark(final Bookmark bookmark) throws BookmarksException {
		bookmarkDatabase.modify(bookmarksTreeModifier -> {
			BookmarkFolder folder = defaultBookmarkFolderManager.getDefaultFolder();
			if (folder == null) {
				folder = bookmarksTreeModifier.getCurrentTree().getRootFolder();
			}
			bookmarksTreeModifier.addBookmarks(folder.getId(), Arrays.asList(bookmark));
		});
	}

}
