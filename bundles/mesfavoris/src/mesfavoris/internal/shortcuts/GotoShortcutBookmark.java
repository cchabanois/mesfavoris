package mesfavoris.internal.shortcuts;

import org.eclipse.ui.IWorkbenchWindow;

import mesfavoris.bookmarktype.IBookmarkLocation;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.model.Bookmark;
import mesfavoris.service.IBookmarksService;

public class GotoShortcutBookmark implements IGotoBookmark {

	protected final IBookmarksService bookmarksService;

	public GotoShortcutBookmark() {
		this.bookmarksService = BookmarksPlugin.getDefault().getBookmarksService();
	}

	@Override
	public boolean gotoBookmark(IWorkbenchWindow window, Bookmark bookmark, IBookmarkLocation bookmarkLocation) {
		if (!(bookmarkLocation instanceof ShortcutBookmarkLocation)) {
			return false;
		}
		ShortcutBookmarkLocation shortcutBookmarkLocation = (ShortcutBookmarkLocation) bookmarkLocation;
		if (bookmarksService.getBookmarksTree().getBookmark(shortcutBookmarkLocation.getBookmarkId()) == null) {
			return false;
		}
		if (window.getActivePage() == null) {
			return false;
		}
		bookmarksService.showInBookmarksView(window.getActivePage(), shortcutBookmarkLocation.getBookmarkId(), false);
		return true;
	}

}
