package mesfavoris.internal.markers;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

import mesfavoris.bookmarktype.IBookmarkLocation;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.model.Bookmark;

public class GotoFileMarkerBookmark implements IGotoBookmark {

	public GotoFileMarkerBookmark() {
	}

	@Override
	public boolean gotoBookmark(IWorkbenchWindow window, Bookmark bookmark, IBookmarkLocation bookmarkLocation) {
		if (!(bookmarkLocation instanceof FileMarkerBookmarkLocation)) {
			return false;
		}
		FileMarkerBookmarkLocation fileMarkerBookmarkLocation = (FileMarkerBookmarkLocation) bookmarkLocation;
		try {
			IEditorPart editorPart = IDE.openEditor(window.getActivePage(), fileMarkerBookmarkLocation.getMarker(),
					true);
			return editorPart != null;
		} catch (PartInitException e) {
			return false;
		}
	}

}
