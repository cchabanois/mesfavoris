package mesfavoris.texteditor.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
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
import mesfavoris.texteditor.internal.TextEditorBookmarkLocationProvider.TextEditorBookmarkLocation;
import mesfavoris.texteditor.placeholders.PathPlaceholderResolver;
import mesfavoris.texteditor.text.matching.DocumentFuzzySearcher;

public class GotoWorkspaceFileBookmark implements IGotoBookmark {
	private final TextEditorBookmarkLocationProvider textEditorBookmarkLocationProvider;

	public GotoWorkspaceFileBookmark() {
		this(new TextEditorBookmarkLocationProvider(new PathPlaceholderResolver(Activator.getPathPlaceholdersStore())));
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
		int lineNumber = location.getLineNumber() == null ? -1 : location.getLineNumber();
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
