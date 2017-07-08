package mesfavoris.internal.handlers;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import mesfavoris.model.BookmarkId;

public class AddShortcutBookmarkHandler extends AbstractAddBookmarkHandler {

	protected BookmarkPartOperationContext getOperationContext(ExecutionEvent event) {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		ISelection selection;
		if (part != null) {
			// see bug #375220 or #282969 to understand why we don't use
			// HandlerUtil.getCurrentSelection(event);
			selection = part.getSite().getSelectionProvider().getSelection();
		} else {
			selection = HandlerUtil.getCurrentSelection(event);
		}
		return new BookmarkPartOperationContext(part, selection);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		BookmarkPartOperationContext operationContext = getOperationContext(event);
		if (operationContext == null) {
			return null;
		}
		BookmarkId bookmarkId = addBookmark(operationContext);
		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
		if (page != null) {
			displayBookmarkInBookmarksView(page, bookmarkId);
		}
		return null;
	}	
	
	@Override
	public boolean isEnabled() {
		List<BookmarkId> bookmarkIds = getAsBookmarkIds(getSelection());
		return bookmarkIds.size() == 1;
	}
	
}
