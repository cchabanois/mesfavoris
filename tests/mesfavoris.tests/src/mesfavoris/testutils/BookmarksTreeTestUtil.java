package mesfavoris.testutils;

import org.chabanois.mesfavoris.model.Bookmark;
import org.chabanois.mesfavoris.model.BookmarkFolder;
import org.chabanois.mesfavoris.model.BookmarksTree;

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
