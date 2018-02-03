package mesfavoris.internal.handlers;

import java.util.Optional;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import mesfavoris.internal.snippets.Snippet;
import mesfavoris.model.BookmarkId;

public class PasteAsSnippetHandler extends AbstractAddBookmarkHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		Display display = part != null ? part.getSite().getShell().getDisplay()
				: PlatformUI.getWorkbench().getDisplay();
		ISelection selection = getSnippetAsSelection(display);
		if (selection.isEmpty()) {
			return null;
		}
		BookmarkPartOperationContext operationContext = new BookmarkPartOperationContext(part, selection);

		BookmarkId bookmarkId = addBookmark(operationContext);
		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
		if (page != null) {
			displayBookmarkInBookmarksView(page, bookmarkId);
		}
		return null;
	}

	private ISelection getSnippetAsSelection(Display display) {
		IStructuredSelection[] result = new IStructuredSelection[1];
		display.syncExec(() -> { 
			Optional<String> text = getTextFromClipboard(display);
			if (text.isPresent()) {
				result[0] = new StructuredSelection(new Snippet(text.get()));
			} else {
				result[0] = new StructuredSelection();
			}
		});
		return result[0];
	}

	private Optional<String> getTextFromClipboard(Display display) {
		Clipboard clipboard = new Clipboard(display);
		try {
			String text = (String) clipboard.getContents(TextTransfer.getInstance());
			return Optional.ofNullable(text);
		} finally {
			clipboard.dispose();
		}
	}

}
