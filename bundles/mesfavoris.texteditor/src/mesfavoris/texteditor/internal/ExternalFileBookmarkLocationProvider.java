package mesfavoris.texteditor.internal;

import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_FILE_PATH;

import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import mesfavoris.model.Bookmark;
import mesfavoris.texteditor.Activator;
import mesfavoris.texteditor.TextEditorBookmarkProperties;
import mesfavoris.texteditor.placeholders.PathPlaceholderResolver;
import mesfavoris.texteditor.text.DocumentUtils;

public class ExternalFileBookmarkLocationProvider extends AbstractFileBookmarkLocationProvider {

	private final PathPlaceholderResolver pathPlaceholderResolver;

	public ExternalFileBookmarkLocationProvider() {
		this(new PathPlaceholderResolver(Activator.getPathPlaceholdersStore()));
	}

	public ExternalFileBookmarkLocationProvider(PathPlaceholderResolver pathPlaceholderResolver) {
		this.pathPlaceholderResolver = pathPlaceholderResolver;
	}

	@Override
	public ExternalFileBookmarkLocation getBookmarkLocation(Bookmark bookmark, IProgressMonitor monitor) {
		IPath filePath = getFilePath(bookmark);
		if (filePath == null || !filePath.toFile().exists()) {
			return null;
		}
		String lineContent = bookmark.getPropertyValue(TextEditorBookmarkProperties.PROP_LINE_CONTENT);
		Integer lineNumber = getExpectedLineNumber(bookmark);
		Integer lineOffset = null;
		Optional<IDocument> document = getDocument(filePath);
		if (lineContent != null && document.isPresent()) {
			lineNumber = getLineNumber(document.get(), lineNumber, lineContent, monitor);
		}
		if (document.isPresent() && lineNumber != null) {
			lineOffset = getLineOffset(document.get(), lineNumber);
		}
		return new ExternalFileBookmarkLocation(filePath, lineNumber, lineOffset);
	}

	private Integer getLineOffset(IDocument document, int lineNumber) {
		try {
			return document.getLineOffset(lineNumber);
		} catch (BadLocationException e) {
			return null;
		}
	}	
	
	private IPath getFilePath(Bookmark bookmark) {
		String nonExpandedFilePath = bookmark.getPropertyValue(PROP_FILE_PATH);
		IPath filePath = nonExpandedFilePath != null ? pathPlaceholderResolver.expand(nonExpandedFilePath) : null;
		return filePath;
	}
	
	private Optional<IDocument> getDocument(IPath filePath) {
		try {
			return Optional.of(DocumentUtils.getDocument(filePath));
		} catch (CoreException e) {
			StatusHelper.logWarn("Could not get document", e);
			return Optional.empty();
		}
	}	
	
}
