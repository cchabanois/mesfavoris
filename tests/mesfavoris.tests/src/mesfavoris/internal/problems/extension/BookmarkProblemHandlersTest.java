package mesfavoris.internal.problems.extension;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import mesfavoris.problems.BookmarkProblem;

public class BookmarkProblemHandlersTest {

	@Test
	public void testBookmarkProblemHandlers() {
		// Given
		BookmarkProblemHandlers bookmarkProblemHandlers = new BookmarkProblemHandlers();

		// Then
		assertNotNull(bookmarkProblemHandlers.getBookmarkProblemHandler(BookmarkProblem.TYPE_CANNOT_GOTOBOOKMARK));
		assertNotNull(bookmarkProblemHandlers.getBookmarkProblemHandler(BookmarkProblem.TYPE_LOCAL_PATH_SHARED));
		assertNotNull(bookmarkProblemHandlers.getBookmarkProblemHandler(BookmarkProblem.TYPE_PLACEHOLDER_UNDEFINED));
		assertNotNull(bookmarkProblemHandlers.getBookmarkProblemHandler(BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE));
	}

}
