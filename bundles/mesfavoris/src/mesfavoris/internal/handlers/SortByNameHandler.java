package mesfavoris.internal.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import mesfavoris.BookmarksException;
import mesfavoris.handlers.AbstractBookmarkHandler;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkFolder;

public class SortByNameHandler extends AbstractBookmarkHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		BookmarkFolder parent = null;
		if (selection.isEmpty()) {
			parent = bookmarksService.getBookmarksTree().getRootFolder();
		} else {
			Bookmark bookmark = (Bookmark) selection.getFirstElement();
			if (!(bookmark instanceof BookmarkFolder)) {
				return null;
			}
			parent = (BookmarkFolder) bookmark;
		}
		try {
			bookmarksService.sortByName(parent.getId());
		} catch (BookmarksException e) {
			throw new ExecutionException("Could not sort by name", e);
		}
		return null;
	}
}
