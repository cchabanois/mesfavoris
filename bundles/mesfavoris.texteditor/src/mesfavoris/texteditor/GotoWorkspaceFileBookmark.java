package mesfavoris.texteditor;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.model.Bookmark;
import mesfavoris.texteditor.TextEditorBookmarkLocationProvider.TextEditorBookmarkLocation;
import mesfavoris.texteditor.placeholders.PathPlaceholderResolver;

public class GotoWorkspaceFileBookmark implements IGotoBookmark {
	private final TextEditorBookmarkLocationProvider textEditorBookmarkLocationProvider;

	public GotoWorkspaceFileBookmark() {
		this(new TextEditorBookmarkLocationProvider(
				new PathPlaceholderResolver(Activator.getPathPlaceholdersStore())));
	}

	public GotoWorkspaceFileBookmark(TextEditorBookmarkLocationProvider textEditorBookmarkLocationProvider) {
		this.textEditorBookmarkLocationProvider = textEditorBookmarkLocationProvider;
	}

	@Override
	public boolean gotoBookmark(IWorkbenchWindow window, Bookmark bookmark) {
		TextEditorBookmarkLocation location = textEditorBookmarkLocationProvider.findLocation(bookmark);
		if (location == null || location.getWorkspaceFile() == null) {
			return false;
		}
		IFile file = location.getWorkspaceFile();
		IEditorPart editorPart = openEditor(window, file);
		if (editorPart == null) {
			return false;
		}
		if (!(editorPart instanceof ITextEditor)) {
			return false;
		}
		ITextEditor textEditor = (ITextEditor) editorPart;
		Integer lineNumber = location.getLineNumber();
		if (lineNumber == null) {
			return true;
		}
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
