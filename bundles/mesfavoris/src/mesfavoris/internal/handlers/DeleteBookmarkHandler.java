package mesfavoris.internal.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import mesfavoris.BookmarksException;
import mesfavoris.handlers.AbstractBookmarkHandler;

public class DeleteBookmarkHandler extends AbstractBookmarkHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		if (selection.isEmpty()) {
			return null;
		}
		try {
			bookmarksService.deleteBookmarks(getAsBookmarkIds(selection), true);
		} catch (BookmarksException e) {
			throw new ExecutionException("Could not delete bookmark", e);
		}
		return null;
	}

}
