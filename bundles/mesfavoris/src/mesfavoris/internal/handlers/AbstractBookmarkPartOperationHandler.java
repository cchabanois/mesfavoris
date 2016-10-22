package mesfavoris.internal.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

import mesfavoris.commons.core.AdapterUtils;
import mesfavoris.handlers.AbstractBookmarkHandler;

public abstract class AbstractBookmarkPartOperationHandler extends AbstractBookmarkHandler {

	protected BookmarkPartOperationContext getOperationContext(ExecutionEvent event) {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part == null) {
			return null;
		}
		ISelection selection;
		if (part instanceof IEditorPart) {
			ITextEditor textEditor = AdapterUtils.getAdapter(part, ITextEditor.class);
			if (textEditor == null) {
				return null;
			}
			selection = textEditor.getSelectionProvider().getSelection();
			part = textEditor;
		} else if (part instanceof IViewPart) {
			selection = HandlerUtil.getCurrentSelection(event);
		} else {
			return null;
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
