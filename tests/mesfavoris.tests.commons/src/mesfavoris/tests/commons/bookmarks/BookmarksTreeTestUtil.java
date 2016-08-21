package mesfavoris.tests.commons.bookmarks;

import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarksTree;

public class BookmarksTreeTestUtil {

	public static BookmarkFolder getBookmarkFolder(BookmarksTree bookmarksTree, int... indexes) {
		return (BookmarkFolder) getBookmark(bookmarksTree, indexes);
	}

	public static Bookmark getBookmark(BookmarksTree bookmarksTree, int... indexes) {
		Bookmark bookmark = bookmarksTree.getRootFolder();
		for (int i : indexes) {
			bookmark = bookmarksTree.getChildren(bookmark.getId()).get(i);
		}
		return bookmark;
	}	
	
}
