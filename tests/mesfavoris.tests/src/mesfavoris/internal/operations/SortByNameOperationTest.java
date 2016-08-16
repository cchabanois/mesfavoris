package mesfavoris.internal.operations;

import static mesfavoris.testutils.BookmarkBuilder.bookmark;
import static mesfavoris.testutils.BookmarkBuilder.bookmarkFolder;
import static mesfavoris.testutils.BookmarksTreeBuilder.bookmarksTree;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.Status;
import org.junit.Before;
import org.junit.Test;

import mesfavoris.BookmarksException;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.testutils.BookmarksTreeBuilder;
import mesfavoris.validation.IBookmarkModificationValidator;

public class SortByNameOperationTest {
	private BookmarkDatabase bookmarkDatabase;
	private IBookmarkModificationValidator bookmarkModificationValidator = mock(IBookmarkModificationValidator.class);
	private SortByNameOperation operation;

	@Before
	public void setUp() {
		bookmarkDatabase = new BookmarkDatabase("testId", getInitialTree());
		when(bookmarkModificationValidator.validateModification(any(BookmarksTree.class), any(BookmarkId.class)))
				.thenReturn(Status.OK_STATUS);
		operation = new SortByNameOperation(bookmarkDatabase, bookmarkModificationValidator);
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
