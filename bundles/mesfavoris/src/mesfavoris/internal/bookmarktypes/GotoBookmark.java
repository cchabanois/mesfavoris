package mesfavoris.internal.bookmarktypes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IWorkbenchWindow;

import mesfavoris.BookmarksPlugin;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.internal.markers.BookmarksMarkers;
import mesfavoris.model.Bookmark;

public class GotoBookmark implements IGotoBookmark {
	private final List<IGotoBookmark> gotoBookmarks;
	private final BookmarksMarkers bookmarksMarkers;
	
	public GotoBookmark(List<IGotoBookmark> gotoBookmarks) {
		this.gotoBookmarks = new ArrayList<IGotoBookmark>(gotoBookmarks);
		this.bookmarksMarkers = BookmarksPlugin.getBookmarksMarkers();
	}

	@Override
	public boolean gotoBookmark(IWorkbenchWindow window, Bookmark bookmark) {
		for (IGotoBookmark gotoBookmark : gotoBookmarks) {
			if (gotoBookmark.gotoBookmark(window, bookmark)) {
				addMarkerIfMissing(bookmark);
				return true;
			}
		}
		return false;
	}

	private void addMarkerIfMissing(Bookmark bookmark) {
		IMarker marker = bookmarksMarkers.findMarker(bookmark.getId());
		if (marker == null) {
			bookmarksMarkers.refreshMarker(bookmark.getId());
		}
	}

}
