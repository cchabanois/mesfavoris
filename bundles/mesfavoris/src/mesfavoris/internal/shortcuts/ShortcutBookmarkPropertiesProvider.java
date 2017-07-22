package mesfavoris.internal.shortcuts;

import static mesfavoris.bookmarktype.BookmarkPropertiesProviderUtil.getFirstElement;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

import mesfavoris.bookmarktype.AbstractBookmarkPropertiesProvider;
import mesfavoris.model.Bookmark;

public class ShortcutBookmarkPropertiesProvider extends AbstractBookmarkPropertiesProvider {

	@Override
	public void addBookmarkProperties(Map<String, String> bookmarkProperties, IWorkbenchPart part, ISelection selection,
			IProgressMonitor monitor) {
		Object selected = getFirstElement(selection);
		if (!(selected instanceof Bookmark)) {
			return;
		}
		Bookmark bookmark = (Bookmark) selected;
		putIfAbsent(bookmarkProperties, Bookmark.PROPERTY_NAME,
				bookmark.getPropertyValue(Bookmark.PROPERTY_NAME) + " shortcut");
		putIfAbsent(bookmarkProperties, ShortcutBookmarkProperties.PROP_BOOKMARK_ID, bookmark.getId().toString());
	}

}
