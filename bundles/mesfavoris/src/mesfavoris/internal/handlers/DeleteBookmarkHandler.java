package mesfavoris.internal.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import mesfavoris.BookmarksException;
import mesfavoris.BookmarksPlugin;
import mesfavoris.internal.operations.DeleteBookmarksOperation;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.validation.BookmarkModificationValidator;

public class DeleteBookmarkHandler extends AbstractBookmarkHandler {
	private final BookmarkDatabase bookmarkDatabase;
	private final BookmarkModificationValidator bookmarkModificationValidator;

	public DeleteBookmarkHandler() {
		this.bookmarkDatabase = BookmarksPlugin.getBookmarkDatabase();
		this.bookmarkModificationValidator = new BookmarkModificationValidator(
				BookmarksPlugin.getRemoteBookmarksStoreManager());
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		if (selection.isEmpty()) {
			return null;
		}
		try {
			DeleteBookmarksOperation operation = new DeleteBookmarksOperation(bookmarkDatabase,
					bookmarkModificationValidator);
			operation.deleteBookmarks(getAsBookmarkIds(selection));
		} catch (BookmarksException e) {
			throw new ExecutionException("Could not delete bookmark", e);
		}
		return null;
	}

}
