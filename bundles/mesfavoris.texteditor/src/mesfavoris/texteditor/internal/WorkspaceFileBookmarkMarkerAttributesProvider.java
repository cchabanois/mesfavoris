package mesfavoris.texteditor.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;

import mesfavoris.bookmarktype.AbstractBookmarkMarkerPropertiesProvider;
import mesfavoris.bookmarktype.BookmarkMarkerDescriptor;
import mesfavoris.bookmarktype.IBookmarkLocation;
import mesfavoris.model.Bookmark;

public class WorkspaceFileBookmarkMarkerAttributesProvider extends AbstractBookmarkMarkerPropertiesProvider {

	private final WorkspaceFileBookmarkLocationProvider workspaceFileBookmarkLocationProvider;

	public WorkspaceFileBookmarkMarkerAttributesProvider() {
		this(new WorkspaceFileBookmarkLocationProvider());
	}

	public WorkspaceFileBookmarkMarkerAttributesProvider(
			WorkspaceFileBookmarkLocationProvider textEditorBookmarkLocationProvider) {
		this.workspaceFileBookmarkLocationProvider = textEditorBookmarkLocationProvider;
	}

	@Override
	public BookmarkMarkerDescriptor getMarkerDescriptor(Bookmark bookmark, IProgressMonitor monitor) {
		IBookmarkLocation location = workspaceFileBookmarkLocationProvider.getBookmarkLocation(bookmark, monitor);
		if (!(location instanceof WorkspaceFileBookmarkLocation)) {
			return null;
		}
		WorkspaceFileBookmarkLocation workspaceFileBookmarkLocation = (WorkspaceFileBookmarkLocation) location;
		if (workspaceFileBookmarkLocation.getLineNumber() == null) {
			return null;
		}
		Map attributes = new HashMap();
		attributes.put(IMarker.LINE_NUMBER, new Integer(workspaceFileBookmarkLocation.getLineNumber() + 1));
		if (workspaceFileBookmarkLocation.getLineOffset() != null) {
			attributes.put(IMarker.CHAR_START, Integer.valueOf(workspaceFileBookmarkLocation.getLineOffset()));
			attributes.put(IMarker.CHAR_END, Integer.valueOf(workspaceFileBookmarkLocation.getLineOffset()));
		}
		getMessage(bookmark).ifPresent(message -> attributes.put(IMarker.MESSAGE, message));
		return new BookmarkMarkerDescriptor(workspaceFileBookmarkLocation.getWorkspaceFile(), attributes);
	}

}
