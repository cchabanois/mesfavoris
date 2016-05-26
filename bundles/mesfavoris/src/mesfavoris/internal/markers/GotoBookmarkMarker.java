package mesfavoris.internal.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

import mesfavoris.BookmarksPlugin;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.model.Bookmark;

public class GotoBookmarkMarker implements IGotoBookmark {
	private final BookmarksMarkers bookmarksMarkers;
	
	public GotoBookmarkMarker() {
		bookmarksMarkers = BookmarksPlugin.getBookmarksMarkers();
	}
	
	@Override
	public boolean gotoBookmark(IWorkbenchWindow window, Bookmark bookmark) {
		IMarker marker = bookmarksMarkers.findMarker(bookmark.getId());
		if (marker == null) {
			return false;
		}
		try {
			IEditorPart editorPart = IDE.openEditor(window.getActivePage(), marker, false);
			return editorPart != null;
		} catch (PartInitException e) {
			return false;
		}
	}
	
}
