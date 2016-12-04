package mesfavoris.internal.service.operations;

import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmark;
import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmarkFolder;
import static mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder.bookmarksTree;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import mesfavoris.BookmarksException;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarksTree;
import mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder;

public class SortByNameOperationTest {
	private BookmarkDatabase bookmarkDatabase;
	private SortByNameOperation operation;

	@Before
	public void setUp() {
		bookmarkDatabase = new BookmarkDatabase("testId", getInitialTree());
		operation = new SortByNameOperation(bookmarkDatabase);
	}

	@Test
	public void testSortByName() throws BookmarksException {
		// When
		operation.sortByName(bookmarkDatabase.getBookmarksTree().getRootFolder().getId());

		// Then
		BookmarksTree expectedTree = bookmarksTree("rootFolder").addBookmarks("rootFolder", bookmarkFolder("a"),
				bookmarkFolder("b"), bookmarkFolder("e"), bookmark("c"), bookmark("d")).build();
		assertEquals(expectedTree.toString(), bookmarkDatabase.getBookmarksTree().toString());
	}

	private BookmarksTree getInitialTree() {
		BookmarksTreeBuilder bookmarksTreeBuilder = bookmarksTree("rootFolder");
		bookmarksTreeBuilder.addBookmarks("rootFolder", bookmarkFolder("b"), bookmarkFolder("a"), bookmark("c"),
				bookmarkFolder("e"), bookmark("d"));
		return bookmarksTreeBuilder.build();
	}

}
