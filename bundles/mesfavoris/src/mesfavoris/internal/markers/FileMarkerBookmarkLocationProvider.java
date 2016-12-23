package mesfavoris.internal.markers;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;

import mesfavoris.bookmarktype.IBookmarkLocation;
import mesfavoris.bookmarktype.IBookmarkLocationProvider;
import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.markers.IBookmarksMarkers;
import mesfavoris.model.Bookmark;

public class FileMarkerBookmarkLocationProvider implements IBookmarkLocationProvider {
	private final IBookmarksMarkers bookmarksMarkers;
	
	public FileMarkerBookmarkLocationProvider() {
		bookmarksMarkers = BookmarksPlugin.getDefault().getBookmarksMarkers();
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
