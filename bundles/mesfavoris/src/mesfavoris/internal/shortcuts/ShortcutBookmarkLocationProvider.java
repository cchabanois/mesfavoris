package mesfavoris.internal.shortcuts;

import org.eclipse.core.runtime.IProgressMonitor;

import mesfavoris.bookmarktype.IBookmarkLocation;
import mesfavoris.bookmarktype.IBookmarkLocationProvider;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;

public class ShortcutBookmarkLocationProvider implements IBookmarkLocationProvider {

	@Override
	public IBookmarkLocation getBookmarkLocation(Bookmark bookmark, IProgressMonitor monitor) {
		String bookmarkIdAsString = bookmark.getPropertyValue(ShortcutBookmarkProperties.PROP_BOOKMARK_ID);
		if (bookmarkIdAsString == null) {
			return null;
		}
		return new ShortcutBookmarkLocation(new BookmarkId(bookmarkIdAsString));
	}

}
