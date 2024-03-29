package mesfavoris.internal.service.operations;

import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmark;
import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmarkFolder;
import static mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder.bookmarksTree;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import mesfavoris.BookmarksException;
import mesfavoris.MesFavoris;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder;

public class AddBookmarksTreeOperationTest {
	private BookmarkDatabase bookmarkDatabase;
	private AddBookmarksTreeOperation operation;

	@Before
	public void setUp() {
		this.bookmarkDatabase = new BookmarkDatabase("test", createBookmarksTree());
		this.operation = new AddBookmarksTreeOperation(bookmarkDatabase);
	}

	@Test
	public void testAddBookmarksTree() throws BookmarksException {
		// Given
		BookmarksTree sourceBookmarksTree = bookmarksTree("folder3")
				.addBookmarks("folder3", bookmarkFolder("folder31"), bookmarkFolder("folder32"), bookmark("bookmark31"))
				.build();

		// When
		operation.addBookmarksTree(new BookmarkId("root"), sourceBookmarksTree, (bookmarksTree) -> {
		});

		// Then
		assertEquals(sourceBookmarksTree.toString(),
				bookmarkDatabase.getBookmarksTree().subTree(new BookmarkId("folder3")).toString());
	}

	@Test
	public void testCannotAddBookmarksTreeWithIdConflict() throws BookmarksException {
		// Given
		BookmarksTree sourceBookmarksTree = bookmarksTree("folder3").addBookmarks("folder3", bookmarkFolder("folder31"),
				bookmarkFolder("folder32"), bookmark("bookmark31"), bookmark("bookmark11")).build();

		// When
		operation.addBookmarksTree(new BookmarkId("root"), sourceBookmarksTree, (bookmarksTree) -> {
		});

		// Then
		// we use NonExistingBookmarkIdProvider when copying bookmarks.
		assertNotNull(bookmarkDatabase.getBookmarksTree().getBookmark(new BookmarkId("folder3")));
	}

	private BookmarksTree createBookmarksTree() {
		BookmarksTreeBuilder bookmarksTreeBuilder = bookmarksTree("root");
		bookmarksTreeBuilder.addBookmarks("root", bookmarkFolder("folder1"), bookmarkFolder("folder2"),
				bookmarkFolder(MesFavoris.DEFAULT_BOOKMARKFOLDER_ID, "default"));
		bookmarksTreeBuilder.addBookmarks("folder1", bookmarkFolder("folder11"), bookmark("bookmark11"),
				bookmark("bookmark12").withProperty(Bookmark.PROPERTY_COMMENT, "comment for bookmark12")
						.withProperty("customProperty", "custom value"));

		return bookmarksTreeBuilder.build();
	}

}
