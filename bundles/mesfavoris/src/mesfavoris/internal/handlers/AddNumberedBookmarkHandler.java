package mesfavoris.internal.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;

import mesfavoris.BookmarksPlugin;
import mesfavoris.internal.numberedbookmarks.BookmarkNumber;
import mesfavoris.internal.numberedbookmarks.NumberedBookmarks;
import mesfavoris.internal.operations.AddNumberedBookmarkOperation;
import mesfavoris.model.BookmarkId;

public class AddNumberedBookmarkHandler extends AbstractAddBookmarkHandler {
	private static final String NUMBER_PARAM = "mesfavoris.command.addNumberedFavori.numberParameter";
	private final NumberedBookmarks numberedBookmarks;

	public AddNumberedBookmarkHandler() {
		super();
		this.numberedBookmarks = BookmarksPlugin.getNumberedBookmarks();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		BookmarkCreationOperationContext operationContext = getOperationContext(event);
		if (operationContext == null) {
			return null;
		}
		BookmarkNumber bookmarkNumber = getBookmarkNumber(event);
		BookmarkId bookmarkId = addBookmark(operationContext);
		AddNumberedBookmarkOperation operation = new AddNumberedBookmarkOperation(numberedBookmarks);
		operation.addNumberedBookmark(bookmarkId, bookmarkNumber);
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
