package mesfavoris.texteditor.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import mesfavoris.bookmarktype.IBookmarkLocation;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.model.Bookmark;
import mesfavoris.texteditor.TextEditorUtils;

public class GotoWorkspaceFileBookmark implements IGotoBookmark {

	@Override
	public boolean gotoBookmark(IWorkbenchWindow window, Bookmark bookmark, IBookmarkLocation bookmarkLocation) {
		if (!(bookmarkLocation instanceof WorkspaceFileBookmarkLocation)) {
			return false;
		}
		WorkspaceFileBookmarkLocation workspaceFileBookmarkLocation = (WorkspaceFileBookmarkLocation) bookmarkLocation;
		IFile file = workspaceFileBookmarkLocation.getWorkspaceFile();
		IEditorPart editorPart = openEditor(window, file);
		if (editorPart == null) {
			return false;
		}
		if (!(editorPart instanceof ITextEditor)) {
			return false;
		}
		ITextEditor textEditor = (ITextEditor) editorPart;
		if (workspaceFileBookmarkLocation.getLineNumber() != null) {
			return gotoLine(textEditor, workspaceFileBookmarkLocation.getLineNumber());
		}
		return true;
	}

	private boolean gotoLine(ITextEditor textEditor, int lineNumber) {
		try {
			TextEditorUtils.gotoLine(textEditor, lineNumber);
			return true;
		} catch (BadLocationException e) {
			return false;
		}
	}

	private IEditorPart openEditor(IWorkbenchWindow window, IFile file) {
		if (!file.exists()) {
			return null;
		}
		IWorkbenchPage page = window.getActivePage();
		try {
			IEditorPart editorPart = IDE.openEditor(page, file);
			return editorPart;
		} catch (PartInitException e) {
			return null;
		}
	}

}
