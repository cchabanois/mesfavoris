package mesfavoris.tests.commons.bookmarks;

import static mesfavoris.MesFavoris.getBookmarkDatabase;
import static mesfavoris.MesFavoris.getBookmarksService;

import java.util.Arrays;
import java.util.stream.Collectors;

import mesfavoris.BookmarksException;
import mesfavoris.MesFavoris;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.service.IBookmarksService;

public class MainBookmarkDatabaseHelper {

	public static void addBookmark(BookmarkId parentId, Bookmark... bookmark) throws BookmarksException {
		getBookmarkDatabase()
				.modify(bookmarksTreeModifier -> bookmarksTreeModifier.addBookmarks(parentId, Arrays.asList(bookmark)));
	}

	public static void deleteAllBookmarksExceptDefaultBookmarkFolder() throws BookmarksException {
		IBookmarksService bookmarksService = getBookmarksService();
		BookmarkId rootFolderId = bookmarksService.getBookmarksTree().getRootFolder().getId();
		bookmarksService.deleteBookmarks(
				bookmarksService.getBookmarksTree().getChildren(rootFolderId).stream().map(bookmark -> bookmark.getId())
						.filter(bookmarkId -> !bookmarkId.equals(MesFavoris.DEFAULT_BOOKMARKFOLDER_ID))
						.collect(Collectors.toList()),
				true);
	}

	public static void deleteBookmark(BookmarkId bookmarkId) throws BookmarksException {
		getBookmarkDatabase().modify(bookmarksTreeModifier -> bookmarksTreeModifier.deleteBookmark(bookmarkId, true));

	}

	public static BookmarkId getBookmarksRootFolderId() {
		return getBookmarksTree().getRootFolder().getId();
	}

	public static BookmarksTree getBookmarksTree() {
		return getBookmarkDatabase().getBookmarksTree();
	}

}
