package mesfavoris.internal.handlers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressService;

import mesfavoris.BookmarksException;
import mesfavoris.BookmarksPlugin;
import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.internal.operations.AddBookmarkOperation;
import mesfavoris.internal.operations.ShowInBookmarksViewOperation;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.workspace.DefaultBookmarkFolderManager;

public class AddBookmarkHandler extends AbstractBookmarkCreationHandler {
	private final DefaultBookmarkFolderManager defaultBookmarkFolderManager;
	private final IBookmarkPropertiesProvider bookmarkPropertiesProvider;
	private final BookmarkDatabase bookmarkDatabase;
	private final ShowInBookmarksViewOperation showInBookmarksViewOperation;

	public AddBookmarkHandler() {
		this.bookmarkDatabase = BookmarksPlugin.getBookmarkDatabase();
		this.bookmarkPropertiesProvider = BookmarksPlugin.getBookmarkPropertiesProvider();
		this.defaultBookmarkFolderManager = BookmarksPlugin.getDefaultBookmarkFolderManager();
		this.showInBookmarksViewOperation = new ShowInBookmarksViewOperation(bookmarkDatabase);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		BookmarkCreationOperationContext operationContext = getOperationContext(event);
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

	private BookmarkId addBookmark(BookmarkCreationOperationContext operationContext) throws ExecutionException {
		BookmarkId[] bookmarkId = new BookmarkId[1];
		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		try {
			progressService.busyCursorWhile(monitor -> {
				AddBookmarkOperation addBookmarkOperation = new AddBookmarkOperation(bookmarkDatabase,
						bookmarkPropertiesProvider, defaultBookmarkFolderManager);
				try {
					bookmarkId[0] = addBookmarkOperation.addBookmark(operationContext.part, operationContext.selection,
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
	
	private void displayBookmarkInBookmarksView(IWorkbenchPage page, BookmarkId bookmarkId) {
		if (page == null) {
			return;
		}
		showInBookmarksViewOperation.showInBookmarksView(page, bookmarkId);
	}

}
