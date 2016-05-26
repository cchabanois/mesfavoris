package mesfavoris.texteditor;

import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_FILE_PATH;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_LINE_NUMBER;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.model.Bookmark;
import mesfavoris.texteditor.placeholders.PathPlaceholderResolver;

public class GotoExternalFileBookmark implements IGotoBookmark {
	private final PathPlaceholderResolver pathPlaceholderResolver;

	public GotoExternalFileBookmark() {
		this(new PathPlaceholderResolver(Activator.getPathPlaceholdersStore()));
	}

	public GotoExternalFileBookmark(PathPlaceholderResolver pathPlaceholderResolver) {
		this.pathPlaceholderResolver = pathPlaceholderResolver;
	}

	@Override
	public boolean gotoBookmark(IWorkbenchWindow window, Bookmark bookmark) {
		String nonExpandedFilePath = bookmark.getPropertyValue(PROP_FILE_PATH);
		IPath filePath = nonExpandedFilePath != null ? pathPlaceholderResolver.expand(nonExpandedFilePath)
				: null;
		if (filePath == null) {
			return false;
		}
		IEditorPart editorPart = openEditor(window, filePath);
		if (editorPart == null) {
			return false;
		}
		if (!(editorPart instanceof ITextEditor)) {
			return false;
		}
		ITextEditor textEditor = (ITextEditor) editorPart;
		String lineNumberAsString = bookmark.getPropertyValue(PROP_LINE_NUMBER);
		if (lineNumberAsString == null) {
			return true;
		}
		int lineNumber = Integer.parseInt(lineNumberAsString);
		try {
			TextEditorUtils.gotoLine(textEditor, lineNumber);
			return true;
		} catch (BadLocationException e) {
			return false;
		}
	}

	private IEditorPart openEditor(IWorkbenchWindow window, IPath filePath) {
		IFileStore fileStore = EFS.getLocalFileSystem().getStore(filePath);
		IFileInfo fetchInfo = fileStore.fetchInfo();
		if (fetchInfo.isDirectory() || !fetchInfo.exists()) {
			return null;
		}
		IWorkbenchPage page = window.getActivePage();
		try {
			IEditorPart editorPart = IDE.openEditorOnFileStore(page, fileStore);
			return editorPart;
		} catch (PartInitException e) {
			return null;
		}
	}

}
