package mesfavoris.texteditor.internal;

import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_FILE_PATH;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_LINE_NUMBER;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_WORKSPACE_PATH;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import mesfavoris.model.Bookmark;
import mesfavoris.texteditor.TextEditorBookmarkProperties;
import mesfavoris.texteditor.placeholders.PathPlaceholderResolver;
import mesfavoris.texteditor.text.DocumentUtils;
import mesfavoris.texteditor.text.matching.DocumentFuzzySearcher;

public class TextEditorBookmarkLocationProvider {
	private final PathPlaceholderResolver pathPlaceholderResolver;

	public TextEditorBookmarkLocationProvider(PathPlaceholderResolver pathPlaceholderResolver) {
		this.pathPlaceholderResolver = pathPlaceholderResolver;
	}

	public TextEditorBookmarkLocation findLocation(Bookmark bookmark, IProgressMonitor monitor) {
		IFile workspaceFile = getWorkspaceFile(bookmark);
		String nonExpandedFilePath = bookmark.getPropertyValue(PROP_FILE_PATH);
		IPath filePath = nonExpandedFilePath != null ? pathPlaceholderResolver.expand(nonExpandedFilePath) : null;
		if (workspaceFile == null && filePath == null) {
			return null;
		}
		if (filePath == null) {
			filePath = workspaceFile.getLocation();
		}
		String lineContent = bookmark.getPropertyValue(TextEditorBookmarkProperties.PROP_LINE_CONTENT);
		Integer lineNumber = getExpectedLineNumber(bookmark);
		if (lineContent != null && filePath != null) {
			lineNumber = getLineNumber(filePath, getExpectedLineNumber(bookmark), lineContent,
					monitor);
		}
		return new TextEditorBookmarkLocation(workspaceFile, filePath, lineNumber);
	}

	private IFile getWorkspaceFile(Bookmark bookmark) {
		String workspacePath = bookmark.getPropertyValue(PROP_WORKSPACE_PATH);
		if (workspacePath == null) {
			return null;
		}
		Path path = new Path(workspacePath);
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		return file;
	}

	private Integer getExpectedLineNumber(Bookmark bookmark) {
		String expectedLineNumberAsString = bookmark.getPropertyValue(PROP_LINE_NUMBER);
		if (expectedLineNumberAsString == null) {
			return null;
		}
		return Integer.parseInt(expectedLineNumberAsString);
	}

	private Integer getLineNumber(IPath fileSystemPath, Integer expectedLineNumber, String lineContent,
			IProgressMonitor monitor) {
		try {
			IDocument document = DocumentUtils.getDocument(fileSystemPath);
			DocumentFuzzySearcher searcher = new DocumentFuzzySearcher(document);
			IRegion region;
			if (expectedLineNumber == null) {
				region = new Region(0, document.getLength());
			} else  {
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

	public static class TextEditorBookmarkLocation {
		private final IFile workspaceFile;
		private final IPath fileSystemPath;
		private final Integer lineNumber;

		public TextEditorBookmarkLocation(IFile workspaceFile, IPath fileSystemPath, Integer lineNumber) {
			this.workspaceFile = workspaceFile;
			this.fileSystemPath = fileSystemPath;
			this.lineNumber = lineNumber;
		}

		public IFile getWorkspaceFile() {
			return workspaceFile;
		}

		public IPath getFileSystemPath() {
			return fileSystemPath;
		}

		public Integer getLineNumber() {
			return lineNumber;
		}

	}

}
