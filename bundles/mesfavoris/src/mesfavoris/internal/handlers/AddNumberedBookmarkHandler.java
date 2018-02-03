package mesfavoris.internal.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;

import mesfavoris.internal.numberedbookmarks.BookmarkNumber;
import mesfavoris.model.BookmarkId;

public class AddNumberedBookmarkHandler extends AbstractAddBookmarkHandler {
	private static final String NUMBER_PARAM = "mesfavoris.command.addNumberedFavori.numberParameter";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		BookmarkPartOperationContext operationContext = getOperationContext(event);

		BookmarkNumber bookmarkNumber = getBookmarkNumber(event);
		BookmarkId bookmarkId = addBookmark(operationContext);
		bookmarksService.addNumberedBookmark(bookmarkId, bookmarkNumber);
		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
		if (page != null) {
			displayBookmarkInBookmarksView(page, bookmarkId);
		}
		return null;
	}

	private BookmarkNumber getBookmarkNumber(ExecutionEvent event) {
		String parameter = event.getParameter(NUMBER_PARAM);
		return BookmarkNumber.valueOf(parameter);
	}

	
}
