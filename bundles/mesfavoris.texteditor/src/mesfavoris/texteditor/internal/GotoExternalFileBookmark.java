package mesfavoris.texteditor.internal;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
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

public class GotoExternalFileBookmark implements IGotoBookmark {

	@Override
	public boolean gotoBookmark(IWorkbenchWindow window, Bookmark bookmark, IBookmarkLocation bookmarkLocation) {
		if (!(bookmarkLocation instanceof ExternalFileBookmarkLocation)) {
			return false;
		}
		ExternalFileBookmarkLocation externalFileBookmarkLocation = (ExternalFileBookmarkLocation) bookmarkLocation;
		IEditorPart editorPart = openEditor(window, externalFileBookmarkLocation.getFileSystemPath());
		if (editorPart == null) {
			return false;
		}
		if (!(editorPart instanceof ITextEditor)) {
			return false;
		}
		ITextEditor textEditor = (ITextEditor) editorPart;
		if (externalFileBookmarkLocation.getLineNumber() != null) {
			return gotoLine(textEditor, externalFileBookmarkLocation.getLineNumber());
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
