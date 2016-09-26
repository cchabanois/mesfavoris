package mesfavoris.internal.handlers;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import mesfavoris.BookmarksException;
import mesfavoris.BookmarksPlugin;
import mesfavoris.handlers.AbstractBookmarkHandler;
import mesfavoris.internal.service.operations.CutBookmarkOperation;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.validation.BookmarkModificationValidator;

public class CutBookmarkHandler extends AbstractBookmarkHandler {
	private final BookmarkDatabase bookmarkDatabase;
	private final BookmarkModificationValidator bookmarkModificationValidator;

	public CutBookmarkHandler() {
		this.bookmarkDatabase = BookmarksPlugin.getBookmarkDatabase();
		this.bookmarkModificationValidator = new BookmarkModificationValidator(
				BookmarksPlugin.getRemoteBookmarksStoreManager());
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		if (selection.isEmpty()) {
			return null;
		}
		try {
			CutBookmarkOperation operation = new CutBookmarkOperation(bookmarkDatabase, bookmarkModificationValidator);
			operation.cutToClipboard(getAsBookmarkIds(selection));
		} catch (BookmarksException e) {
			throw new ExecutionException("Could not cut bookmarks to clipboard", e);
		}
		return null;
	}

	@Override
	public boolean isEnabled() {
		List<BookmarkId> bookmarkIds = getAsBookmarkIds(getSelection());
		CutBookmarkOperation operation = new CutBookmarkOperation(bookmarkDatabase, bookmarkModificationValidator);
		return operation.hasDuplicatedBookmarksInSelection(bookmarkDatabase.getBookmarksTree(), bookmarkIds);
	}
}
