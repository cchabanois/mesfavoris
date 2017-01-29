package mesfavoris.internal.problems;

import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmark;
import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmarkFolder;
import static mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder.bookmarksTree;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.Maps;

import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.problems.BookmarkProblem;
import mesfavoris.problems.BookmarkProblem.Severity;
import mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder;

public class BookmarkProblemsDatabaseTest {
	private BookmarkProblemsDatabase bookmarkProblemsDatabase;
	private BookmarkDatabase bookmarkDatabase;
	private File file;
	private IEventBroker eventBroker;

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Before
	public void setUp() throws Exception {
		this.eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);
		bookmarkDatabase = new BookmarkDatabase("main", getInitialTree());
		file = temporaryFolder.newFile();
		bookmarkProblemsDatabase = new BookmarkProblemsDatabase(eventBroker, bookmarkDatabase, file);
		bookmarkProblemsDatabase.init();
	}

	@After
	public void tearDown() throws Exception {
		bookmarkProblemsDatabase.close();
	}

	@Test
	public void testAddBookmarkProblem() {
		// Given
		BookmarkId bookmarkId = new BookmarkId();
		BookmarkProblem problem = gotoBookmarkProblem(bookmarkId);

		// When
		bookmarkProblemsDatabase.add(problem);

		// Then
		assertThat(bookmarkProblemsDatabase.getBookmarkProblems(bookmarkId)).containsExactly(problem);
		assertThat(bookmarkProblemsDatabase.size()).isEqualTo(1);
	}
	
	@Test
	public void testBookmarkDeleted() throws Exception {
		// Given
		BookmarkId bookmarkId = new BookmarkId("bookmark1");
		BookmarkProblem problem1 = gotoBookmarkProblem(bookmarkId);
		BookmarkProblem problem2 = placeHolderUndefinedProblem(bookmarkId);
		bookmarkProblemsDatabase.add(problem1);
		bookmarkProblemsDatabase.add(problem2);
		
		// When
		bookmarkDatabase.modify(bookmarksTreeModifier -> bookmarksTreeModifier.deleteBookmark(bookmarkId, false));

		// Then
		assertThat(bookmarkProblemsDatabase.getBookmarkProblems(bookmarkId)).isEmpty();
		assertThat(bookmarkProblemsDatabase.size()).isEqualTo(0);
	}	
	
	private BookmarksTree getInitialTree() {
		BookmarksTreeBuilder bookmarksTreeBuilder = bookmarksTree("rootFolder");
		bookmarksTreeBuilder.addBookmarks("rootFolder", bookmarkFolder("bookmarkFolder1"),
				bookmarkFolder("bookmarkFolder2"));
		bookmarksTreeBuilder.addBookmarks("bookmarkFolder1", bookmark("bookmark1"), bookmark("bookmark2"));
		bookmarksTreeBuilder.addBookmarks("bookmarkFolder2", bookmarkFolder("bookmarkFolder3"), bookmark("bookmark3"),
				bookmark("bookmark4"));
		bookmarksTreeBuilder.addBookmarks("bookmarkFolder3", bookmark("bookmark5"));

		return bookmarksTreeBuilder.build();
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
