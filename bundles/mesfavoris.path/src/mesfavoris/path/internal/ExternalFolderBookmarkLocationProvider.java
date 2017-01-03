package mesfavoris.path.internal;

import static mesfavoris.path.PathBookmarkProperties.PROP_FOLDER_PATH;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import mesfavoris.MesFavoris;
import mesfavoris.bookmarktype.IBookmarkLocationProvider;
import mesfavoris.model.Bookmark;
import mesfavoris.placeholders.IPathPlaceholderResolver;

public class ExternalFolderBookmarkLocationProvider implements IBookmarkLocationProvider {

	private final IPathPlaceholderResolver pathPlaceholderResolver;

	public ExternalFolderBookmarkLocationProvider() {
		this(MesFavoris.getPathPlaceholderResolver());
	}

	public ExternalFolderBookmarkLocationProvider(IPathPlaceholderResolver pathPlaceholderResolver) {
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
