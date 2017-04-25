package mesfavoris.internal.problems;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.ImmutableMap;

import mesfavoris.model.BookmarkId;
import mesfavoris.problems.BookmarkProblem;

public class BookmarkProblemsPersisterTest {
	private BookmarkProblems bookmarkProblems = new BookmarkProblems();

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private BookmarkProblemsPersister bookmarkProblemsPersister;

	@Before
	public void setUp() throws IOException {
		bookmarkProblemsPersister = new BookmarkProblemsPersister(temporaryFolder.newFile());
	}

	@Test
	public void testSaveLoadBookmarkProblems() throws IOException {
		// Given
		BookmarkId bookmarkId1 = new BookmarkId();
		BookmarkId bookmarkId2 = new BookmarkId();
		BookmarkProblem problem1 = new BookmarkProblem(bookmarkId1, BookmarkProblem.TYPE_CANNOT_GOTOBOOKMARK);
		BookmarkProblem problem2 = new BookmarkProblem(bookmarkId1, BookmarkProblem.TYPE_PLACEHOLDER_UNDEFINED,
				ImmutableMap.of("placeholder", "MY_PLACEHOLDER"));
		BookmarkProblem problem3 = new BookmarkProblem(bookmarkId1, BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE,
				ImmutableMap.of("prop1", "value1", "prop2", "value2"));
		BookmarkProblem problem4 = new BookmarkProblem(bookmarkId2, BookmarkProblem.TYPE_CANNOT_GOTOBOOKMARK);
		bookmarkProblems = bookmarkProblems.add(problem1).add(problem2).add(problem3).add(problem4);

		// When
		bookmarkProblemsPersister.save(bookmarkProblems, new NullProgressMonitor());
		BookmarkProblems loadedBookmarkProblems = bookmarkProblemsPersister.load();

		// Then
		assertThat(loadedBookmarkProblems.getBookmarksWithProblems()).containsExactlyInAnyOrder(bookmarkId1,
				bookmarkId2);
		assertThat(loadedBookmarkProblems.getBookmarkProblem(bookmarkId1, BookmarkProblem.TYPE_CANNOT_GOTOBOOKMARK))
				.hasValueSatisfying(problem -> assertThat(problem.getProperties()).isEmpty());
		assertThat(loadedBookmarkProblems.getBookmarkProblem(bookmarkId1, BookmarkProblem.TYPE_PLACEHOLDER_UNDEFINED))
				.hasValueSatisfying(
						problem -> assertThat(problem.getProperties()).containsEntry("placeholder", "MY_PLACEHOLDER"));
		assertThat(loadedBookmarkProblems.getBookmarkProblem(bookmarkId1, BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE))
				.hasValueSatisfying(problem -> assertThat(problem.getProperties()).containsEntry("prop1", "value1")
						.containsEntry("prop2", "value2"));
		assertThat(loadedBookmarkProblems.getBookmarkProblem(bookmarkId2, BookmarkProblem.TYPE_CANNOT_GOTOBOOKMARK))
				.hasValueSatisfying(problem -> assertThat(problem.getProperties()).isEmpty());
	}

}
