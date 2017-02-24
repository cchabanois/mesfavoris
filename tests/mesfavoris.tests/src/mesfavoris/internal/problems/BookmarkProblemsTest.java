package mesfavoris.internal.problems;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Test;

import com.google.common.collect.Maps;

import mesfavoris.model.BookmarkId;
import mesfavoris.problems.BookmarkProblem;
import mesfavoris.problems.BookmarkProblem.Severity;

public class BookmarkProblemsTest {
	private BookmarkProblems bookmarkProblems = new BookmarkProblems();

	@Test
	public void testAddBookmarkProblem() {
		// Given
		BookmarkId bookmarkId = new BookmarkId();
		BookmarkProblem problem = gotoBookmarkProblem(bookmarkId);

		// When
		bookmarkProblems = bookmarkProblems.add(problem);

		// Then
		assertThat(bookmarkProblems.getBookmarkProblems(bookmarkId)).containsExactly(problem);
		assertThat(bookmarkProblems.size()).isEqualTo(1);
	}

	@Test
	public void testDeleteBookmarkProblem() {
		// Given
		BookmarkId bookmarkId = new BookmarkId();
		BookmarkProblem problem = gotoBookmarkProblem(bookmarkId);
		bookmarkProblems = bookmarkProblems.add(problem);

		// When
		bookmarkProblems = bookmarkProblems.delete(problem);

		// Then
		assertThat(bookmarkProblems.getBookmarkProblems(bookmarkId)).isEmpty();
		assertThat(bookmarkProblems.size()).isEqualTo(0);
	}

	@Test
	public void testDelete() {
		// Given
		BookmarkId bookmarkId = new BookmarkId();
		BookmarkProblem problem1 = gotoBookmarkProblem(bookmarkId);
		BookmarkProblem problem2 = placeHolderUndefinedProblem(bookmarkId);
		bookmarkProblems = bookmarkProblems.add(problem1).add(problem2);

		// When
		bookmarkProblems = bookmarkProblems.delete(bookmarkId);

		// Then
		assertThat(bookmarkProblems.getBookmarkProblems(bookmarkId)).isEmpty();
		assertThat(bookmarkProblems.size()).isEqualTo(0);
	}

	@Test
	public void testReplaceProblemOfSameType() {
		// Given
		BookmarkId bookmarkId = new BookmarkId();
		BookmarkProblem problem1 = gotoBookmarkProblem(bookmarkId);
		BookmarkProblem problem2 = gotoBookmarkProblem(bookmarkId);

		// When
		bookmarkProblems = bookmarkProblems.add(problem1).add(problem2);

		// Then
		assertThat(bookmarkProblems.getBookmarkProblems(bookmarkId))
				.hasOnlyOneElementSatisfying(problem -> assertThat(problem).isSameAs(problem2));
	}

	@Test
	public void testBookmarkProblemsIteratorWhenNoProblems() {
		assertFalse(bookmarkProblems.iterator().hasNext());
		assertThatThrownBy(() -> {
			bookmarkProblems.iterator().next();
		}).isInstanceOf(NoSuchElementException.class);
	}

	@Test
	public void testBookmarkProblemsIterator() {
		// Given
		BookmarkId bookmarkId1 = new BookmarkId();
		BookmarkId bookmarkId2 = new BookmarkId();
		BookmarkProblem problem1 = gotoBookmarkProblem(bookmarkId1);
		BookmarkProblem problem2 = gotoBookmarkProblem(bookmarkId2);
		BookmarkProblem problem3 = placeHolderUndefinedProblem(bookmarkId1);
		bookmarkProblems = bookmarkProblems.add(problem1).add(problem2).add(problem3);
		
		// When
		Iterator<BookmarkProblem> it = bookmarkProblems.iterator();
		
		// Then
		assertThat(it).containsExactlyInAnyOrder(problem1, problem2, problem3);
	}	
	
	private BookmarkProblem gotoBookmarkProblem(BookmarkId bookmarkId) {
		return new BookmarkProblem(bookmarkId, BookmarkProblem.TYPE_CANNOT_GOTOBOOKMARK, Severity.ERROR,
				Maps.newHashMap());

	}

	private BookmarkProblem placeHolderUndefinedProblem(BookmarkId bookmarkId) {
		return new BookmarkProblem(bookmarkId, BookmarkProblem.TYPE_PLACEHOLDER_UNDEFINED, Severity.WARNING,
				Maps.newHashMap());

	}

}
