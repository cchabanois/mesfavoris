package mesfavoris.internal.handlers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressService;

import mesfavoris.BookmarksException;
import mesfavoris.commons.core.AdapterUtils;
import mesfavoris.handlers.AbstractBookmarkHandler;
import mesfavoris.internal.StatusHelper;
import mesfavoris.model.Bookmark;

public class GotoBookmarkHandler extends AbstractBookmarkHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		Bookmark bookmark = getSelectedBookmark(selection);
		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		try {
			progressService.busyCursorWhile(monitor -> {
				try {
					bookmarksService.gotoBookmark(bookmark.getId(), monitor);
				} catch (BookmarksException e) {
					throw new InvocationTargetException(e);
				}

			});
		} catch (InvocationTargetException e) {
			StatusHelper.showError("Could not go to bookmark", e.getCause(), false);
		} catch (InterruptedException e) {
			throw new ExecutionException("Could not go to bookmark : cancelled");
		}
		return null;
	}

	private Bookmark getSelectedBookmark(IStructuredSelection selection) {
		if (selection.isEmpty()) {
			return null;
		}
		return AdapterUtils.getAdapter(selection.getFirstElement(), Bookmark.class);
	}

}
