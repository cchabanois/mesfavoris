package mesfavoris.internal.handlers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressService;

import mesfavoris.BookmarksException;
import mesfavoris.handlers.AbstractBookmarkHandler;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;

public class PasteBookmarkHandler extends AbstractBookmarkHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		if (selection.size() != 1 || !(selection.getFirstElement() instanceof BookmarkFolder)) {
			return null;
		}
		BookmarkFolder bookmarkFolder = (BookmarkFolder) selection.getFirstElement();
		paste(Display.getCurrent(), bookmarkFolder.getId());

		return null;
	}

	private void paste(Display display, BookmarkId bookmarkFolderId) throws ExecutionException {
		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		try {
			progressService.busyCursorWhile(monitor -> {
				try {
					bookmarksService.paste(display, bookmarkFolderId, monitor);
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
