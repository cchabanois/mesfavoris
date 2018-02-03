package mesfavoris.internal.handlers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressService;

import mesfavoris.BookmarksException;
import mesfavoris.internal.views.BookmarksView;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;

public class UpdateBookmarkHandler extends AbstractBookmarkPartOperationHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		BookmarkPartOperationContext operationContext = getOperationContext(event);

		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
		if (page == null) {
			return null;
		}
		BookmarkId bookmarkId = getSelectedBookmarkId(page);
		if (bookmarkId == null) {
			return null;
		}
		updateBookmark(bookmarkId, operationContext);
		return null;
	}

	private void updateBookmark(BookmarkId bookmarkId, BookmarkPartOperationContext operationContext) throws ExecutionException {
		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		try {
			progressService.busyCursorWhile(monitor -> {
				try {
					bookmarksService.updateBookmark(bookmarkId, operationContext.part, operationContext.selection,
							monitor);
				} catch (BookmarksException e) {
					throw new InvocationTargetException(e);
				}

			});
		} catch (InvocationTargetException e) {
			throw new ExecutionException("Could not update bookmark", e.getCause());
		} catch (InterruptedException e) {
			throw new ExecutionException("Could not update bookmark : cancelled");
		}
	}	
	
	private BookmarkId getSelectedBookmarkId(IWorkbenchPage page) {
		BookmarksView bookmarksView = (BookmarksView) page.findView(BookmarksView.ID);
		if (bookmarksView == null) {
			return null;
		}
		ISelection selection = bookmarksView.getViewSite().getSelectionProvider().getSelection();
		if (!(selection instanceof IStructuredSelection)) {
			return null;
		}
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		Object selectedElement = structuredSelection.getFirstElement();
		if (!(selectedElement instanceof Bookmark) || (selectedElement instanceof BookmarkFolder)) {
			return null;
		}
		Bookmark bookmark = (Bookmark) selectedElement;
		return bookmark.getId();
	}

}
