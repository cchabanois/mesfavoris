package mesfavoris.internal.markers;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;

import mesfavoris.BookmarksPlugin;
import mesfavoris.bookmarktype.IBookmarkLocation;
import mesfavoris.bookmarktype.IBookmarkLocationProvider;
import mesfavoris.model.Bookmark;

public class FileMarkerBookmarkLocationProvider implements IBookmarkLocationProvider {
	private final BookmarksMarkers bookmarksMarkers;
	
	public FileMarkerBookmarkLocationProvider() {
		bookmarksMarkers = BookmarksPlugin.getBookmarksMarkers();
	}
	
	@Override
	public IBookmarkLocation getBookmarkLocation(Bookmark bookmark, IProgressMonitor monitor) {
		IMarker marker = bookmarksMarkers.findMarker(bookmark.getId(), monitor);
		if (marker == null) {
			return null;
		}
		if (!(marker.getResource() instanceof IFile)) {
			return null;
		}
		return new FileMarkerBookmarkLocation(marker);
	}

}
