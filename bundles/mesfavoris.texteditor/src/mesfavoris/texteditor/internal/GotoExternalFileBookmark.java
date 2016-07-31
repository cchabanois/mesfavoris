package mesfavoris.texteditor.internal;

import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_FILE_PATH;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_LINE_NUMBER;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.model.Bookmark;
import mesfavoris.texteditor.Activator;
import mesfavoris.texteditor.TextEditorBookmarkProperties;
import mesfavoris.texteditor.TextEditorUtils;
import mesfavoris.texteditor.placeholders.PathPlaceholderResolver;
import mesfavoris.texteditor.text.matching.DocumentFuzzySearcher;

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
		int lineNumber = getLineNumber(bookmark);
		String lineContent = bookmark.getPropertyValue(TextEditorBookmarkProperties.PROP_LINE_CONTENT);
		if (lineContent == null) {
			if (lineNumber == -1) {
				return false;
			}
			return gotoLine(textEditor, lineNumber);
		}
		IDocumentProvider provider = textEditor.getDocumentProvider();
		IDocument document = provider.getDocument(textEditor.getEditorInput());
		lineNumber = getLineNumber(document, lineNumber, lineContent, new NullProgressMonitor());
		if (lineNumber == -1) {
			return false;
		}
		return gotoLine(textEditor, lineNumber);
	}

	private int getLineNumber(Bookmark bookmark) {
		String lineNumberAsString = bookmark.getPropertyValue(PROP_LINE_NUMBER);
		if (lineNumberAsString == null) {
			return -1;
		}
		return Integer.parseInt(lineNumberAsString);
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
	
	private int getLineNumber(IDocument document, int expectedLineNumber, String lineContent,
			IProgressMonitor monitor) {
		try {
			DocumentFuzzySearcher searcher = new DocumentFuzzySearcher(document);
			IRegion region = getRegionAround(document, expectedLineNumber, 100);
			return searcher.findLineNumber(region, expectedLineNumber, lineContent, monitor);
		} catch (BadLocationException e) {
			return -1;
		}
	}

	private IRegion getRegionAround(IDocument document, int lineNumber, int linesAround) throws BadLocationException {
		int firstLine = lineNumber - linesAround;
		if (firstLine < 0) {
			firstLine = 0;
		}
		int lastLine = lineNumber + linesAround;
		if (lastLine >= document.getNumberOfLines()) {
			lastLine = document.getNumberOfLines() - 1;
		}
		int offset = document.getLineOffset(firstLine);
		int length = document.getLineOffset(lastLine) + document.getLineLength(lastLine) - offset;
		IRegion region = new Region(offset, length);
		return region;
	}

}
