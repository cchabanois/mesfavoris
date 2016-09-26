package mesfavoris.internal.handlers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import mesfavoris.BookmarksException;
import mesfavoris.model.BookmarkId;

public abstract class AbstractAddBookmarkHandler extends AbstractBookmarkCreationHandler {
	
	protected BookmarkId addBookmark(BookmarkCreationOperationContext operationContext) throws ExecutionException {
		BookmarkId[] bookmarkId = new BookmarkId[1];
		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		try {
			progressService.busyCursorWhile(monitor -> {
				try {
					bookmarkId[0] = bookmarksService.addBookmark(operationContext.part, operationContext.selection,
							monitor);
				} catch (BookmarksException e) {
					throw new InvocationTargetException(e);
				}

			});
		} catch (InvocationTargetException e) {
			throw new ExecutionException("Could not add bookmark", e.getCause());
		} catch (InterruptedException e) {
			throw new ExecutionException("Could not add bookmark : cancelled");
		}
		return bookmarkId[0];
	}
	
	protected void displayBookmarkInBookmarksView(IWorkbenchPage page, BookmarkId bookmarkId) {
		if (page == null) {
			return;
		}
		bookmarksService.showInBookmarksView(page, bookmarkId);
	}	
	
}
