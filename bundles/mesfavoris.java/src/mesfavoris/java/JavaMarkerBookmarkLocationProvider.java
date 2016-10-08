package mesfavoris.java;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;

import mesfavoris.BookmarksPlugin;
import mesfavoris.bookmarktype.IBookmarkLocation;
import mesfavoris.bookmarktype.IBookmarkLocationProvider;
import mesfavoris.markers.IBookmarksMarkers;
import mesfavoris.model.Bookmark;

public class JavaMarkerBookmarkLocationProvider implements IBookmarkLocationProvider {
	private final IBookmarksMarkers bookmarksMarkers;
	
	public JavaMarkerBookmarkLocationProvider() {
		bookmarksMarkers = BookmarksPlugin.getBookmarksMarkers();
	}
	
	@Override
	public IBookmarkLocation getBookmarkLocation(Bookmark bookmark, IProgressMonitor monitor) {
		IMarker marker = bookmarksMarkers.findMarker(bookmark.getId(), monitor);
		if (marker == null) {
			return null;
		}
		String handle = marker.getAttribute(JavaMarkerBookmarkLocation.ATT_HANDLE_ID, null);
		if (handle == null) {
			return null;
		}
		return new JavaMarkerBookmarkLocation(marker);
	}

}
