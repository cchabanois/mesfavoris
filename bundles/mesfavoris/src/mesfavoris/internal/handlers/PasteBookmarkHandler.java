package mesfavoris.internal.handlers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressService;

import mesfavoris.BookmarksException;
import mesfavoris.MesFavoris;
import mesfavoris.handlers.AbstractBookmarkHandler;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;

public class PasteBookmarkHandler extends AbstractBookmarkHandler {
	private final BookmarkDatabase bookmarkDatabase;
	
	public PasteBookmarkHandler() {
		this.bookmarkDatabase = MesFavoris.getBookmarkDatabase();
	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		if (selection.size() != 1 || !(selection.getFirstElement() instanceof Bookmark)) {
			return null;
		}
		Bookmark bookmark = (Bookmark)selection.getFirstElement();
		if (bookmark instanceof BookmarkFolder) {
			paste(bookmark.getId());
		} else if (bookmark instanceof Bookmark) {
			BookmarkFolder parentBookmark = bookmarkDatabase.getBookmarksTree().getParentBookmark(bookmark.getId());
			pasteAfter(parentBookmark.getId(), bookmark.getId());
		}		
		return null;
	}

	private void paste(BookmarkId bookmarkFolderId) throws ExecutionException {
		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		try {
			progressService.busyCursorWhile(monitor -> {
				try {
					bookmarksService.paste(bookmarkFolderId, monitor);
				} catch (BookmarksException e) {
					throw new InvocationTargetException(e);
				}

			});
		} catch (InvocationTargetException e) {
			throw new ExecutionException("Could not paste", e.getCause());
		} catch (InterruptedException e) {
			throw new ExecutionException("Could not paste : cancelled");
		}
	}

	private void pasteAfter(BookmarkId bookmarkFolderId, BookmarkId bookmarkId) throws ExecutionException {
		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		try {
			progressService.busyCursorWhile(monitor -> {
				try {
					bookmarksService.pasteAfter(bookmarkFolderId, bookmarkId, monitor);
				} catch (BookmarksException e) {
					throw new InvocationTargetException(e);
				}

			});
		} catch (InvocationTargetException e) {
			throw new ExecutionException("Could not paste", e.getCause());
		} catch (InterruptedException e) {
			throw new ExecutionException("Could not paste : cancelled");
		}
	}	
	
}
