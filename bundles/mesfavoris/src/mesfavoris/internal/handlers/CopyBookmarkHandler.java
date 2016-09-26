package mesfavoris.internal.handlers;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import mesfavoris.handlers.AbstractBookmarkHandler;
import mesfavoris.internal.service.operations.CopyBookmarkOperation;
import mesfavoris.model.BookmarkId;

public class CopyBookmarkHandler extends AbstractBookmarkHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		if (selection.isEmpty()) {
			return null;
		}
		List<BookmarkId> bookmarkIds = getAsBookmarkIds(selection);
		bookmarksService.copyToClipboard(bookmarkIds);
		return null;
	}

	@Override
	public boolean isEnabled() {
		List<BookmarkId> bookmarkIds = getAsBookmarkIds(getSelection());
		CopyBookmarkOperation operation = new CopyBookmarkOperation();
		return !operation.hasDuplicatedBookmarksInSelection(bookmarksService.getBookmarksTree(), bookmarkIds);
	}

}