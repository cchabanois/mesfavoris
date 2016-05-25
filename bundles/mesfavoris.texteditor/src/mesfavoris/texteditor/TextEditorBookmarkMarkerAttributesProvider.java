package mesfavoris.texteditor;

import java.util.HashMap;
import java.util.Map;

import org.chabanois.mesfavoris.bookmarktype.BookmarkMarkerDescriptor;
import org.chabanois.mesfavoris.bookmarktype.IBookmarkMarkerAttributesProvider;
import org.chabanois.mesfavoris.model.Bookmark;
import org.eclipse.core.resources.IMarker;

import mesfavoris.texteditor.TextEditorBookmarkLocationProvider.TextEditorBookmarkLocation;
import mesfavoris.texteditor.placeholders.PathPlaceholderResolver;

public class TextEditorBookmarkMarkerAttributesProvider implements IBookmarkMarkerAttributesProvider {

	private final TextEditorBookmarkLocationProvider textEditorBookmarkLocationProvider;

	public TextEditorBookmarkMarkerAttributesProvider() {
		this(new TextEditorBookmarkLocationProvider(
				new PathPlaceholderResolver(Activator.getPathPlaceholdersStore())));
	}

	public TextEditorBookmarkMarkerAttributesProvider(
			TextEditorBookmarkLocationProvider textEditorBookmarkLocationProvider) {
		this.textEditorBookmarkLocationProvider = textEditorBookmarkLocationProvider;
	}

	@Override
	public BookmarkMarkerDescriptor getMarkerDescriptor(Bookmark bookmark) {
		TextEditorBookmarkLocation location = textEditorBookmarkLocationProvider.findLocation(bookmark);
		if (location == null || location.getWorkspaceFile() == null) {
			return null;
		}
		Map attributes = new HashMap();
		attributes.put(IMarker.LINE_NUMBER, new Integer(location.getLineNumber() + 1));
		return new BookmarkMarkerDescriptor(location.getWorkspaceFile(), attributes);
	}

}
