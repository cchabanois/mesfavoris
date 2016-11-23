package mesfavoris.internal.service.operations;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;

import mesfavoris.BookmarksException;
import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.internal.workspace.DefaultBookmarkFolderProvider;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.validation.IBookmarkModificationValidator;

public class AddBookmarkOperation {
	private final BookmarkDatabase bookmarkDatabase;
	private final DefaultBookmarkFolderProvider defaultBookmarkFolderProvider;
	private final IBookmarkPropertiesProvider bookmarkPropertiesProvider;
	private final IBookmarkModificationValidator bookmarkModificationValidator;

	public AddBookmarkOperation(BookmarkDatabase bookmarkDatabase,
			IBookmarkPropertiesProvider bookmarkPropertiesProvider,
			DefaultBookmarkFolderProvider defaultBookmarkFolderProvider,
			IBookmarkModificationValidator bookmarkModificationValidator) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.defaultBookmarkFolderProvider = defaultBookmarkFolderProvider;
		this.bookmarkPropertiesProvider = bookmarkPropertiesProvider;
		this.bookmarkModificationValidator = bookmarkModificationValidator;
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
		BookmarkId folderId = defaultBookmarkFolderProvider.getDefaultBookmarkFolder(page);
		bookmarkDatabase.modify(bookmarksTreeModifier -> {
			IStatus status = bookmarkModificationValidator.validateModification(bookmarksTreeModifier.getCurrentTree(),
					folderId);
			if (!status.isOK()) {
				throw new BookmarksException(status);
			}
			bookmarksTreeModifier.addBookmarks(folderId, Arrays.asList(bookmark));
		});
	}

}
