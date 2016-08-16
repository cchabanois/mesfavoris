package mesfavoris.internal.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import mesfavoris.BookmarksException;
import mesfavoris.BookmarksPlugin;
import mesfavoris.handlers.AbstractBookmarkHandler;
import mesfavoris.internal.operations.SortByNameOperation;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.validation.BookmarkModificationValidator;
import mesfavoris.validation.IBookmarkModificationValidator;

public class SortByNameHandler extends AbstractBookmarkHandler {
	private final BookmarkDatabase bookmarkDatabase;
	private final IBookmarkModificationValidator bookmarkModificationValidator;

	public SortByNameHandler() {
		this.bookmarkDatabase = BookmarksPlugin.getBookmarkDatabase();
		this.bookmarkModificationValidator = new BookmarkModificationValidator(
				BookmarksPlugin.getRemoteBookmarksStoreManager());
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		BookmarkFolder parent = null;
		if (selection.isEmpty()) {
			parent = bookmarkDatabase.getBookmarksTree().getRootFolder();
		} else {
			Bookmark bookmark = (Bookmark) selection.getFirstElement();
			if (!(bookmark instanceof BookmarkFolder)) {
				return null;
			}
			parent = (BookmarkFolder) bookmark;
		}
		SortByNameOperation operation = new SortByNameOperation(bookmarkDatabase, bookmarkModificationValidator);
		try {
			operation.sortByName(parent.getId());
		} catch (BookmarksException e) {
			throw new ExecutionException("Could not sort by name", e);
		}
		return null;
	}
}
