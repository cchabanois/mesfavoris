package mesfavoris.texteditor.internal;

import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_FILE_PATH;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import mesfavoris.model.Bookmark;
import mesfavoris.texteditor.Activator;
import mesfavoris.texteditor.TextEditorBookmarkProperties;
import mesfavoris.texteditor.placeholders.PathPlaceholderResolver;

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
		if (lineContent != null && filePath != null) {
			lineNumber = getLineNumber(filePath, lineNumber, lineContent, monitor);
		}
		return new ExternalFileBookmarkLocation(filePath, lineNumber);
	}

	private IPath getFilePath(Bookmark bookmark) {
		String nonExpandedFilePath = bookmark.getPropertyValue(PROP_FILE_PATH);
		IPath filePath = nonExpandedFilePath != null ? pathPlaceholderResolver.expand(nonExpandedFilePath) : null;
		return filePath;
	}
	
}
