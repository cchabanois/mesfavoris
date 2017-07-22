package mesfavoris.internal.service.operations.utils;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;

import com.google.common.collect.Lists;

import mesfavoris.BookmarksException;
import mesfavoris.internal.StatusHelper;
import mesfavoris.internal.views.BookmarksView;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import static mesfavoris.internal.Constants.DEFAULT_BOOKMARKFOLDER_ID;

/**
 * Provides the position where to add a new bookmark.
 * 
 * If bookmarks view is opened, the selected bookmark is used to determine the
 * position, otherwise the "default" bookmark folder is returned.
 * 
 * @author cchabanois
 *
 */
public class NewBookmarkPositionProvider implements INewBookmarkPositionProvider {
	private final BookmarkDatabase bookmarkDatabase;

	public NewBookmarkPositionProvider(BookmarkDatabase bookmarkDatabase) {
		this.bookmarkDatabase = bookmarkDatabase;
	}

	@Override
	public NewBookmarkPosition getNewBookmarkPosition(IWorkbenchPage workbenchPage) {
		NewBookmarkPosition bookmarkPosition = getCurrentBookmarkPositionFromBookmarksView(workbenchPage);
		if (bookmarkPosition != null && isModifiable(bookmarkPosition.getParentBookmarkId())) {
			return bookmarkPosition;
		}
		if (!exists(DEFAULT_BOOKMARKFOLDER_ID)) {
			createDefaultBookmarkFolder();
		}
		if (exists(DEFAULT_BOOKMARKFOLDER_ID) && isModifiable(DEFAULT_BOOKMARKFOLDER_ID)) {
			return new NewBookmarkPosition(DEFAULT_BOOKMARKFOLDER_ID);
		}
		return new NewBookmarkPosition(bookmarkDatabase.getBookmarksTree().getRootFolder().getId());
	}

	private void createDefaultBookmarkFolder() {
		try {
			bookmarkDatabase.modify(bookmarksTreeModifier -> {
				BookmarkFolder bookmarkFolder = new BookmarkFolder(DEFAULT_BOOKMARKFOLDER_ID, "default");
				bookmarksTreeModifier.addBookmarksAfter(bookmarksTreeModifier.getCurrentTree().getRootFolder().getId(),
						null, Lists.newArrayList(bookmarkFolder));
			});
		} catch (BookmarksException e) {
			StatusHelper.logWarn("Could not create default folder", e);
		}
	}

	private boolean exists(BookmarkId bookmarkId) {
		return bookmarkDatabase.getBookmarksTree().getBookmark(bookmarkId) != null;
	}

	private boolean isModifiable(BookmarkId bookmarkId) {
		return bookmarkDatabase.getBookmarksModificationValidator()
				.validateModification(bookmarkDatabase.getBookmarksTree(), bookmarkId).isOK();
	}

	private NewBookmarkPosition getCurrentBookmarkPositionFromBookmarksView(IWorkbenchPage page) {
		Object firstSelectedElement = getBookmarksViewSelection(page).getFirstElement();
		if (firstSelectedElement instanceof BookmarkFolder) {
			BookmarkFolder bookmarkFolder = (BookmarkFolder) firstSelectedElement;
			return new NewBookmarkPosition(bookmarkFolder.getId());
		} else if (firstSelectedElement instanceof Bookmark) {
			Bookmark bookmark = (Bookmark) firstSelectedElement;
			BookmarkFolder parentBookmark = bookmarkDatabase.getBookmarksTree().getParentBookmark(bookmark.getId());
			return new NewBookmarkPosition(parentBookmark.getId(), bookmark.getId());
		}
		return null;
	}

	private IStructuredSelection getBookmarksViewSelection(IWorkbenchPage page) {
		BookmarksView bookmarksView = (BookmarksView) page.findView(BookmarksView.ID);
		if (bookmarksView == null) {
			return new StructuredSelection();
		}
		IStructuredSelection[] selection = new IStructuredSelection[1];
		page.getWorkbenchWindow().getShell().getDisplay()
				.syncExec(() -> selection[0] = (IStructuredSelection) bookmarksView.getSite().getSelectionProvider()
						.getSelection());
		return selection[0];
	}

}
