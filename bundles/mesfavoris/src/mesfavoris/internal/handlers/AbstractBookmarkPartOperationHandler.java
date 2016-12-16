package mesfavoris.internal.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import mesfavoris.handlers.AbstractBookmarkHandler;
import mesfavoris.internal.views.BookmarksView;

public abstract class AbstractBookmarkPartOperationHandler extends AbstractBookmarkHandler {

	protected BookmarkPartOperationContext getOperationContext(ExecutionEvent event) {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		ISelection selection;
		if (part != null) {
			// see bug #375220 or #282969 to understand why we don't use
			// HandlerUtil.getCurrentSelection(event);
			selection = part.getSite().getSelectionProvider().getSelection();
		} else {
			selection = HandlerUtil.getCurrentSelection(event);
		}
		if (part instanceof BookmarksView) {
			part = ((BookmarksView) part).getPreviousActivePart();
			if (part != null) {
				selection = part.getSite().getSelectionProvider().getSelection();
			}
		}
		return new BookmarkPartOperationContext(part, selection);
	}

	protected static class BookmarkPartOperationContext {
		protected final IWorkbenchPart part;
		protected final ISelection selection;

		public BookmarkPartOperationContext(IWorkbenchPart part, ISelection selection) {
			this.part = part;
			this.selection = selection;
		}
	}
}
