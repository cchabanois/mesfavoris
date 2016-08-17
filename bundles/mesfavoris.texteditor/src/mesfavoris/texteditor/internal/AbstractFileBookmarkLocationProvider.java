package mesfavoris.texteditor.internal;

import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_LINE_NUMBER;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import mesfavoris.bookmarktype.IBookmarkLocationProvider;
import mesfavoris.model.Bookmark;
import mesfavoris.texteditor.text.DocumentUtils;
import mesfavoris.texteditor.text.matching.DocumentFuzzySearcher;

public abstract class AbstractFileBookmarkLocationProvider implements IBookmarkLocationProvider {

	protected Integer getExpectedLineNumber(Bookmark bookmark) {
		String expectedLineNumberAsString = bookmark.getPropertyValue(PROP_LINE_NUMBER);
		if (expectedLineNumberAsString == null) {
			return null;
		}
		return Integer.parseInt(expectedLineNumberAsString);
	}	
	
	protected Integer getLineNumber(IPath fileSystemPath, Integer expectedLineNumber, String lineContent,
			IProgressMonitor monitor) {
		try {
			IDocument document = DocumentUtils.getDocument(fileSystemPath);
			DocumentFuzzySearcher searcher = new DocumentFuzzySearcher(document);
			IRegion region;
			if (expectedLineNumber == null) {
				region = new Region(0, document.getLength());
			} else {
				region = getRegionAround(document, expectedLineNumber, 1000);
			}
			int lineNumber = searcher.findLineNumber(region, expectedLineNumber == null ? -1 : expectedLineNumber,
					lineContent, monitor);
			if (lineNumber == -1) {
				return null;
			}
			return lineNumber;
		} catch (BadLocationException e) {
			return null;
		} catch (CoreException e) {
			StatusHelper.logWarn("Could not get line number", e);
			return null;
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
