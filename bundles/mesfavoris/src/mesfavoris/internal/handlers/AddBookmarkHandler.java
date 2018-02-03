package mesfavoris.internal.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;

import mesfavoris.model.BookmarkId;

public class AddBookmarkHandler extends AbstractAddBookmarkHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		BookmarkPartOperationContext operationContext = getOperationContext(event);

		BookmarkId bookmarkId = addBookmark(operationContext);
		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
		if (page != null) {
			displayBookmarkInBookmarksView(page, bookmarkId);
		}
		return null;
	}

}
