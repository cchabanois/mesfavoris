package mesfavoris.tests.commons.bookmarks;

import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;

public class BookmarksTreeGenerator {
	private final int levels;
	private final int bookmarkFoldersPerLevel;
	private final int bookmarksPerLevel;
	private final IDGenerator idGenerator;

	public BookmarksTreeGenerator(IDGenerator idGenerator, int levels, int bookmarkFoldersPerLevel, int bookmarksPerLevel) {
		this.levels = levels;
		this.bookmarkFoldersPerLevel = bookmarkFoldersPerLevel;
		this.bookmarksPerLevel = bookmarksPerLevel;
		this.idGenerator = idGenerator;
	}

	public BookmarksTree build() {
		BookmarkFolder rootFolder = new BookmarkFolder(idGenerator.newId(), "root");
		BookmarksTree bookmarksTree = new BookmarksTree(rootFolder);
		bookmarksTree = buildBookmarksTree(bookmarksTree, rootFolder, 1);
		return bookmarksTree;
	}

	private BookmarksTree buildBookmarksTree(BookmarksTree bookmarksTree, BookmarkFolder parentFolder, int level) {
		if (level == levels) {
			return bookmarksTree;
		}
		String parentName = parentFolder.getPropertyValue(Bookmark.PROPERTY_NAME);
		for (int i = 0; i < bookmarkFoldersPerLevel; i++) {
			BookmarkId id = idGenerator.newId();
			BookmarkFolder bookmarkFolder = new BookmarkFolder(id, parentName+"/f"+i);
			bookmarksTree = bookmarksTree.addBookmarks(parentFolder.getId(), Lists.newArrayList(bookmarkFolder));
			bookmarksTree = buildBookmarksTree(bookmarksTree, bookmarkFolder, level + 1);
		}
		for (int i = 0; i < bookmarksPerLevel; i++) {
			BookmarkId id = idGenerator.newId();
			Bookmark bookmark = new Bookmark(id, singleValueMap(Bookmark.PROPERTY_NAME, parentName+"/b"+i));
			bookmarksTree = bookmarksTree.addBookmarks(parentFolder.getId(), Lists.newArrayList(bookmark));
		}
		return bookmarksTree;
	}

	private static Map<String, String> singleValueMap(String name, String value) {
		Map<String, String> map = Maps.newHashMapWithExpectedSize(1);
		map.put(name, value);
		return map;
	}
	
}
