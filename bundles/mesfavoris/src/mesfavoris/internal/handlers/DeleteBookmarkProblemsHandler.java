package mesfavoris.internal.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressService;

import mesfavoris.MesFavoris;
import mesfavoris.handlers.AbstractBookmarkHandler;
import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.StatusHelper;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.problems.IBookmarkProblems;

public class DeleteBookmarkProblemsHandler extends AbstractBookmarkHandler {
	private final BookmarkDatabase bookmarkDatabase;
	private final IBookmarkProblems bookmarkProblems;

	public DeleteBookmarkProblemsHandler() {
		this.bookmarkDatabase = MesFavoris.getBookmarkDatabase();
		this.bookmarkProblems = BookmarksPlugin.getDefault().getBookmarkProblems();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		if (selection.isEmpty()) {
			return null;
		}

		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		try {
			progressService.busyCursorWhile(monitor -> {
				Set<Bookmark> bookmarks = getSelectedBookmarksRecursively(bookmarkDatabase.getBookmarksTree(),
						selection, b->true);
				SubMonitor subMonitor = SubMonitor.convert(monitor, "Delete bookmark problems", bookmarks.size());
				for (Bookmark bookmark : bookmarks) {
					bookmarkProblems.delete(bookmark.getId());
					subMonitor.worked(1);
				}
			});
		} catch (InvocationTargetException e) {
			StatusHelper.showError("Could not delete bookmark problems", e.getCause(), false);
		} catch (InterruptedException e) {
			throw new ExecutionException("Could not delete bookmark problems : cancelled");
		}
		return null;
	}

}
