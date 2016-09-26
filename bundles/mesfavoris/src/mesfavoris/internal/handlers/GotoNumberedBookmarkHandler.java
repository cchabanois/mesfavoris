package mesfavoris.internal.handlers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import mesfavoris.BookmarksException;
import mesfavoris.BookmarksPlugin;
import mesfavoris.StatusHelper;
import mesfavoris.handlers.AbstractBookmarkHandler;
import mesfavoris.internal.numberedbookmarks.BookmarkNumber;

public class GotoNumberedBookmarkHandler extends AbstractBookmarkHandler {
	private static final String NUMBER_PARAM = "mesfavoris.command.gotoNumberedFavori.numberParameter";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		BookmarkNumber bookmarkNumber = getBookmarkNumber(event);
		gotoNumberedBookmark(bookmarkNumber);
		return null;
	}

	private void gotoNumberedBookmark(BookmarkNumber bookmarkNumber) throws ExecutionException {
		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		try {
			progressService.busyCursorWhile(monitor -> {
				try {
					BookmarksPlugin.getBookmarksService().gotoNumberedBookmark(bookmarkNumber, monitor);
				} catch (BookmarksException e) {
					throw new InvocationTargetException(e);
				}

			});
		} catch (InvocationTargetException e) {
			StatusHelper.showError("Could not goto numbered bookmark", e.getCause(), false);
		} catch (InterruptedException e) {
			throw new ExecutionException("Could not goto numbered bookmark : cancelled");
		}
	}		
	
	
	private BookmarkNumber getBookmarkNumber(ExecutionEvent event) {
		String parameter = event.getParameter(NUMBER_PARAM);
		return BookmarkNumber.valueOf(parameter);
	}	
	
}
