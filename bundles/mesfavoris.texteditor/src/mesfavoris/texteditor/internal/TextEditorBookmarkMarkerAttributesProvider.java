package mesfavoris.texteditor.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;

import mesfavoris.bookmarktype.AbstractBookmarkMarkerPropertiesProvider;
import mesfavoris.bookmarktype.BookmarkMarkerDescriptor;
import mesfavoris.bookmarktype.IBookmarkLocation;
import mesfavoris.model.Bookmark;
import mesfavoris.texteditor.Activator;
import mesfavoris.texteditor.placeholders.PathPlaceholderResolver;

public class TextEditorBookmarkMarkerAttributesProvider extends AbstractBookmarkMarkerPropertiesProvider {

	private final TextEditorBookmarkLocationProvider textEditorBookmarkLocationProvider;

	public TextEditorBookmarkMarkerAttributesProvider() {
		this(new TextEditorBookmarkLocationProvider(new PathPlaceholderResolver(Activator.getPathPlaceholdersStore())));
	}

	public TextEditorBookmarkMarkerAttributesProvider(
			TextEditorBookmarkLocationProvider textEditorBookmarkLocationProvider) {
		this.textEditorBookmarkLocationProvider = textEditorBookmarkLocationProvider;
	}

	@Override
	public BookmarkMarkerDescriptor getMarkerDescriptor(Bookmark bookmark, IProgressMonitor monitor) {
		IBookmarkLocation location = textEditorBookmarkLocationProvider.getBookmarkLocation(bookmark, monitor);
		if (!(location instanceof WorkspaceFileBookmarkLocation)) {
			return null;
		}
		WorkspaceFileBookmarkLocation workspaceFileBookmarkLocation = (WorkspaceFileBookmarkLocation) location;
		if (workspaceFileBookmarkLocation.getLineNumber() == null) {
			return null;
		}
		Map attributes = new HashMap();
		attributes.put(IMarker.LINE_NUMBER, new Integer(workspaceFileBookmarkLocation.getLineNumber() + 1));
		getMessage(bookmark).ifPresent(message -> attributes.put(IMarker.MESSAGE, message));
		return new BookmarkMarkerDescriptor(workspaceFileBookmarkLocation.getWorkspaceFile(), attributes);
	}

}
