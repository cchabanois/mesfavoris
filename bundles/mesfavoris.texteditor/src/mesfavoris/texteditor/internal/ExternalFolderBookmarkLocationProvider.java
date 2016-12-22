package mesfavoris.texteditor.internal;

import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_FOLDER_PATH;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import mesfavoris.BookmarksPlugin;
import mesfavoris.model.Bookmark;
import mesfavoris.placeholders.PathPlaceholderResolver;

public class ExternalFolderBookmarkLocationProvider extends AbstractFileBookmarkLocationProvider {

	private final PathPlaceholderResolver pathPlaceholderResolver;

	public ExternalFolderBookmarkLocationProvider() {
		this(new PathPlaceholderResolver(BookmarksPlugin.getPathPlaceholdersStore()));
	}

	public ExternalFolderBookmarkLocationProvider(PathPlaceholderResolver pathPlaceholderResolver) {
		this.pathPlaceholderResolver = pathPlaceholderResolver;
	}

	@Override
	public ExternalFolderBookmarkLocation getBookmarkLocation(Bookmark bookmark, IProgressMonitor monitor) {
		IPath filePath = getFolderPath(bookmark);
		if (filePath == null || !filePath.toFile().exists()) {
			return null;
		}
		return new ExternalFolderBookmarkLocation(filePath);
	}
	
	private IPath getFolderPath(Bookmark bookmark) {
		String nonExpandedFilePath = bookmark.getPropertyValue(PROP_FOLDER_PATH);
		IPath filePath = nonExpandedFilePath != null ? pathPlaceholderResolver.expand(nonExpandedFilePath) : null;
		return filePath;
	}
	
}
