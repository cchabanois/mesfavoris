package mesfavoris.internal.problems.extension;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import mesfavoris.problems.BookmarkProblem;

public class BookmarkProblemDescriptorsTest {
	private BookmarkProblemDescriptors bookmarkProblemDescriptors;
	
	@Before
	public void setUp() {
		bookmarkProblemDescriptors = new BookmarkProblemDescriptors();
	}
	
	@Test
	public void testBookmarkProblemHandlers() {
		// Given

		// Then
		assertHasHandler(BookmarkProblem.TYPE_CANNOT_GOTOBOOKMARK);
		assertHasHandler(BookmarkProblem.TYPE_CANNOT_GOTOBOOKMARK);
		assertHasHandler(BookmarkProblem.TYPE_LOCAL_PATH_SHARED);
		assertHasHandler(BookmarkProblem.TYPE_PLACEHOLDER_UNDEFINED);
		assertHasHandler(BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE);
	}

	private void assertHasHandler(String problemType) {
		assertThat(bookmarkProblemDescriptors.getBookmarkProblemDescriptor(problemType)
				.getBookmarkProblemHandler()).isNotEmpty();
	}
	
}
