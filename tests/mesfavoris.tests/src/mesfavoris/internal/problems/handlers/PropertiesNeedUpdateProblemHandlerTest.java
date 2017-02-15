package mesfavoris.internal.problems.handlers;

import static mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder.bookmarksTree;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.io.File;

import org.assertj.core.util.Lists;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.ImmutableMap;

import mesfavoris.BookmarksException;
import mesfavoris.internal.problems.BookmarkProblemsDatabase;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.problems.BookmarkProblem;
import mesfavoris.problems.BookmarkProblem.Severity;
import mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder;

public class PropertiesNeedUpdateProblemHandlerTest {
	private PropertiesNeedUpdateProblemHandler handler;
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
		handler = new PropertiesNeedUpdateProblemHandler(bookmarkDatabase, bookmarkProblemsDatabase);
	}

	@After
	public void tearDown() throws Exception {
		bookmarkProblemsDatabase.close();
	}

	@Test
	public void testAction() throws BookmarksException {
		// Given
		BookmarkId bookmarkId = new BookmarkId("bookmark1");
		bookmarkDatabase.modify(bookmarksTreeModifier -> {
			bookmarksTreeModifier.addBookmarks(new BookmarkId("rootFolder"), Lists.newArrayList(new Bookmark(bookmarkId,
					ImmutableMap.of(Bookmark.PROPERTY_NAME, "bookmark1", "prop1", "value1", "prop2", "value2"))));
		});
		BookmarkProblem problem = new BookmarkProblem(bookmarkId, BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE,
				Severity.WARNING, ImmutableMap.of("prop2", "newValue2", "prop3", "value3"));
		bookmarkProblemsDatabase.add(problem);

		// When
		handler.handleAction(problem);

		// Then
		assertThat(bookmarkDatabase.getBookmarksTree().getBookmark(bookmarkId).getProperties()).containsExactly(
				entry(Bookmark.PROPERTY_NAME, "bookmark1"), entry("prop1", "value1"), entry("prop2", "newValue2"),
				entry("prop3", "value3"));
	}

	private BookmarksTree getInitialTree() {
		BookmarksTreeBuilder bookmarksTreeBuilder = bookmarksTree("rootFolder");
		return bookmarksTreeBuilder.build();
	}

}
