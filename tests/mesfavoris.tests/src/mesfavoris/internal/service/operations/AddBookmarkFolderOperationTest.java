package mesfavoris.internal.service.operations;

import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmark;
import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmarkFolder;
import static mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder.bookmarksTree;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import mesfavoris.BookmarksException;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;

public class AddBookmarkFolderOperationTest {
	private BookmarkDatabase bookmarkDatabase;
	private BookmarkId rootFolderId;
	private AddBookmarkFolderOperation operation;

	@Before
	public void setUp() {
		this.bookmarkDatabase = new BookmarkDatabase("testId", bookmarksTree("rootFolder").build());
		rootFolderId = bookmarkDatabase.getBookmarksTree().getRootFolder().getId();
		operation = new AddBookmarkFolderOperation(bookmarkDatabase);
	}

	@Test
	public void testAddBookmarkFolderInEmptyFolder() throws Exception {
		// Given
		addBookmark(rootFolderId, bookmarkFolder("folder1").build());

		// When
		operation.addBookmarkFolder(new BookmarkId("folder1"), "folder2");

		// Then
		List<Bookmark> bookmarks = bookmarkDatabase.getBookmarksTree().getChildren(new BookmarkId("folder1"));
		assertEquals(1, bookmarks.size());
		assertEquals("folder2", bookmarks.get(0).getPropertyValue(Bookmark.PROPERTY_NAME));
	}

	@Test
	public void testBookmarkFolderIsAddedBeforeBookmarks() throws Exception {
		// Given
		addBookmark(rootFolderId, bookmarkFolder("folder1").build());
		addBookmark(new BookmarkId("folder1"), bookmark("bookmark1").build(), bookmark("bookmark2").build());

		// When
		operation.addBookmarkFolder(new BookmarkId("folder1"), "folder2");

		// Then
		List<Bookmark> bookmarks = bookmarkDatabase.getBookmarksTree().getChildren(new BookmarkId("folder1"));
		assertEquals(3, bookmarks.size());
		assertEquals("folder2", bookmarks.get(0).getPropertyValue(Bookmark.PROPERTY_NAME));
	}

	@Test
	public void testBookmarkFolderIsAddedAfterExistingBookmarkFolders() throws Exception {
		// Given
		addBookmark(rootFolderId, bookmarkFolder("folder1").build());
		addBookmark(new BookmarkId("folder1"), bookmarkFolder("folder2").build(), bookmark("bookmark1").build(),
				bookmark("bookmark2").build());

		// When
		operation.addBookmarkFolder(new BookmarkId("folder1"), "folder3");

		// Then
		List<Bookmark> bookmarks = bookmarkDatabase.getBookmarksTree().getChildren(new BookmarkId("folder1"));
		assertEquals(4, bookmarks.size());
		assertEquals("folder3", bookmarks.get(1).getPropertyValue(Bookmark.PROPERTY_NAME));
	}

	private void addBookmark(BookmarkId parentId, Bookmark... bookmark) throws BookmarksException {
		bookmarkDatabase
				.modify(bookmarksTreeModifier -> bookmarksTreeModifier.addBookmarks(parentId, Arrays.asList(bookmark)));
	}

}
