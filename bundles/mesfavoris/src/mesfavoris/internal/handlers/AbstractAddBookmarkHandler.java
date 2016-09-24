package mesfavoris.internal.handlers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import mesfavoris.BookmarksException;
import mesfavoris.BookmarksPlugin;
import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.internal.operations.AddBookmarkOperation;
import mesfavoris.internal.operations.ShowInBookmarksViewOperation;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.workspace.DefaultBookmarkFolderProvider;

public abstract class AbstractAddBookmarkHandler extends AbstractBookmarkCreationHandler {
	protected final DefaultBookmarkFolderProvider defaultBookmarkFolderProvider;
	protected final IBookmarkPropertiesProvider bookmarkPropertiesProvider;
	protected final BookmarkDatabase bookmarkDatabase;
	protected final ShowInBookmarksViewOperation showInBookmarksViewOperation;

	public AbstractAddBookmarkHandler() {
		this.bookmarkDatabase = BookmarksPlugin.getBookmarkDatabase();
		this.bookmarkPropertiesProvider = BookmarksPlugin.getBookmarkPropertiesProvider();
		this.defaultBookmarkFolderProvider = BookmarksPlugin.getDefaultBookmarkFolderProvider();
		this.showInBookmarksViewOperation = new ShowInBookmarksViewOperation(bookmarkDatabase);
	}
	
	protected BookmarkId addBookmark(BookmarkCreationOperationContext operationContext) throws ExecutionException {
		BookmarkId[] bookmarkId = new BookmarkId[1];
		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		try {
			progressService.busyCursorWhile(monitor -> {
				AddBookmarkOperation addBookmarkOperation = new AddBookmarkOperation(bookmarkDatabase,
						bookmarkPropertiesProvider, defaultBookmarkFolderProvider);
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
	
	protected void displayBookmarkInBookmarksView(IWorkbenchPage page, BookmarkId bookmarkId) {
		if (page == null) {
			return;
		}
		showInBookmarksViewOperation.showInBookmarksView(page, bookmarkId);
	}	
	
}
