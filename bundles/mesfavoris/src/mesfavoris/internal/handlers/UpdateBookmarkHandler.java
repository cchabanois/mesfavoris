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
import mesfavoris.BookmarksPlugin;
import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.internal.operations.UpdateBookmarkOperation;
import mesfavoris.internal.views.BookmarksView;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;

public class UpdateBookmarkHandler extends AbstractBookmarkCreationHandler {
	private final IBookmarkPropertiesProvider bookmarkPropertiesProvider;
	private final BookmarkDatabase bookmarkDatabase;

	public UpdateBookmarkHandler() {
		this.bookmarkDatabase = BookmarksPlugin.getBookmarkDatabase();
		this.bookmarkPropertiesProvider = BookmarksPlugin.getBookmarkPropertiesProvider();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		BookmarkCreationOperationContext operationContext = getOperationContext(event);
		if (operationContext == null) {
			return null;
		}
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

	private void updateBookmark(BookmarkId bookmarkId, BookmarkCreationOperationContext operationContext) throws ExecutionException {
		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		try {
			progressService.busyCursorWhile(monitor -> {
				try {
					UpdateBookmarkOperation updateBookmarkOperation = new UpdateBookmarkOperation(bookmarkDatabase,
							bookmarkPropertiesProvider);
					updateBookmarkOperation.updateBookmark(bookmarkId, operationContext.part, operationContext.selection,
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
