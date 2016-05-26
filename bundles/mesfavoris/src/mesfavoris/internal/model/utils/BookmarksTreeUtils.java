package mesfavoris.internal.model.utils;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;

public class BookmarksTreeUtils {

	/**
	 * Filter the list of bookmarks and remove those that are already contained
	 * in bookmark folders in the list
	 * 
	 * @param bookmarksTree
	 * @param bookmarkIds
	 * @return
	 */
	public static List<BookmarkId> getUnduplicatedBookmarks(BookmarksTree bookmarksTree, List<BookmarkId> bookmarkIds) {
		List<Bookmark> bookmarks = bookmarkIds.stream().map(b -> bookmarksTree.getBookmark(b))
				.collect(Collectors.toList());
		List<BookmarkFolder> bookmarkFolders = bookmarks.stream().filter(b -> b instanceof BookmarkFolder)
				.map(b -> (BookmarkFolder) b).collect(Collectors.toList());
		return bookmarks.stream().filter(bookmark -> !isBookmarkUnderAny(bookmarksTree, bookmark, bookmarkFolders))
				.map(Bookmark::getId).collect(Collectors.toList());
	}

	/**
	 * Check if a bookmark is under a given bookmark folder
	 * 
	 * @param bookmarksTree
	 * @param bookmarkId
	 * @param bookmarkFolderId
	 * @return
	 */
	public static boolean isBookmarkUnder(BookmarksTree bookmarksTree, BookmarkId bookmarkId,
			BookmarkId bookmarkFolderId) {
		Bookmark bookmark = bookmarksTree.getBookmark(bookmarkId);
		if (bookmark == null) {
			return false;
		}
		if (bookmarkFolderId.equals(bookmarksTree.getRootFolder().getId())) {
			return true;
		}
		Bookmark bookmarkFolder = bookmarksTree.getBookmark(bookmarkFolderId);
		if (bookmarkFolder == null || !(bookmarkFolder instanceof BookmarkFolder)) {
			return false;
		}
		return isBookmarkUnder(bookmarksTree, bookmark, (BookmarkFolder) bookmarkFolder);
	}

	public static Bookmark getBookmarkBefore(BookmarksTree bookmarksTree, BookmarkId bookmarkId, Predicate<Bookmark> filter) {
		Bookmark bookmark = bookmarksTree.getBookmark(bookmarkId);
		if (bookmark == null) {
			return null;
		}
		BookmarkFolder parentFolder = bookmarksTree.getParentBookmark(bookmarkId);
		if (parentFolder == null) {
			return null;
		}
		List<Bookmark> children = bookmarksTree.getChildren(parentFolder.getId());
		int index = children.indexOf(bookmark);
		if (index == 0 || index == -1) {
			return null;
		}
		for (int i = index - 1; i >= 0; i--) {
			Bookmark child = children.get(i);
			if (filter.test(child)) {
				return child;
			}
		}
		return null;
	}	
	
	private static boolean isBookmarkUnderAny(BookmarksTree bookmarksTree, Bookmark bookmark,
			List<BookmarkFolder> bookmarkFolders) {
		for (BookmarkFolder bookmarkFolder : bookmarkFolders) {
			if (isBookmarkUnder(bookmarksTree, bookmark, bookmarkFolder)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isBookmarkUnder(BookmarksTree bookmarksTree, Bookmark bookmark,
			BookmarkFolder bookmarkFolder) {
		if (bookmarksTree.getChildren(bookmarkFolder.getId()).contains(bookmark)) {
			return true;
		}
		BookmarkFolder parent = bookmarksTree.getParentBookmark(bookmark.getId());
		if (parent == null) {
			return false;
		}
		return isBookmarkUnder(bookmarksTree, parent, bookmarkFolder);
	}

}
