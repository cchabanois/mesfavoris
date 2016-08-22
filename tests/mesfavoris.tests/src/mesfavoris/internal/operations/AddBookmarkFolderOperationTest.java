package mesfavoris.internal.operations;

import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.*;
import static mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder.bookmarksTree;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Status;
import org.junit.Before;
import org.junit.Test;

import mesfavoris.BookmarksException;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.validation.IBookmarkModificationValidator;

public class AddBookmarkFolderOperationTest {
	private IBookmarkModificationValidator bookmarkModificationValidator = mock(IBookmarkModificationValidator.class);
	private BookmarkDatabase bookmarkDatabase;
	private BookmarkId rootFolderId;
	private AddBookmarkFolderOperation operation;

	@Before
	public void setUp() {
		when(bookmarkModificationValidator.validateModification(any(BookmarksTree.class), any(BookmarkId.class)))
				.thenReturn(Status.OK_STATUS);
		this.bookmarkDatabase = new BookmarkDatabase("testId", bookmarksTree("rootFolder").build());
		rootFolderId = bookmarkDatabase.getBookmarksTree().getRootFolder().getId();
		operation = new AddBookmarkFolderOperation(bookmarkDatabase, bookmarkModificationValidator);
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
