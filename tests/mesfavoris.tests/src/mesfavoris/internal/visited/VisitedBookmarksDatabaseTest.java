package mesfavoris.internal.visited;

import static mesfavoris.testutils.BookmarksTreeTestUtil.getBookmark;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.ImmutableMap;

import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.testutils.BookmarksTreeBuilder;
import mesfavoris.testutils.IncrementalIDGenerator;
import mesfavoris.topics.BookmarksEvents;

public class VisitedBookmarksDatabaseTest {
	private VisitedBookmarksDatabase visitedBookmarksDatabase;
	private BookmarkDatabase bookmarkDatabase;
	private File file;
	private IEventBroker eventBroker;

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Before
	public void setUp() throws Exception {
		this.eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);
		BookmarksTree bookmarksTree = new BookmarksTreeBuilder(new IncrementalIDGenerator(), 5, 1, 5).build();
		bookmarkDatabase = new BookmarkDatabase("main", bookmarksTree);
		file = temporaryFolder.newFile();
		visitedBookmarksDatabase = new VisitedBookmarksDatabase(eventBroker, bookmarkDatabase, file);
		visitedBookmarksDatabase.init();
	}

	@After
	public void tearDown() throws Exception {
		visitedBookmarksDatabase.close();
	}

	@Test
	public void testVisitBookmark() {
		// Given
		BookmarkId bookmarkId = getBookmark(bookmarkDatabase.getBookmarksTree(), 0, 0, 1).getId();

		// When
		bookmarkVisited(bookmarkId);
		bookmarkVisited(bookmarkId);

		// Then
		assertThat(visitedBookmarksDatabase.getVisitedBookmarks().getMostVisitedBookmarks(5)).containsExactly(bookmarkId);
	}

	@Test
	public void testGetMostVisitedBookmarks() {
		// Given
		BookmarkId bookmarkId1 = getBookmark(bookmarkDatabase.getBookmarksTree(), 0, 0, 1).getId();
		BookmarkId bookmarkId2 = getBookmark(bookmarkDatabase.getBookmarksTree(), 0, 0, 2).getId();
		BookmarkId bookmarkId3 = getBookmark(bookmarkDatabase.getBookmarksTree(), 0, 0, 3).getId();

		// When
		bookmarkVisited(bookmarkId1);
		bookmarkVisited(bookmarkId1);
		bookmarkVisited(bookmarkId3);
		bookmarkVisited(bookmarkId1);
		bookmarkVisited(bookmarkId2);
		bookmarkVisited(bookmarkId2);

		// Then
		assertThat(visitedBookmarksDatabase.getVisitedBookmarks().getMostVisitedBookmarks(5)).containsExactly(bookmarkId1,
				bookmarkId2, bookmarkId3);
	}

	@Test
	public void testGetLatestVisitedBookmarks() throws Exception {
		// Given
		BookmarkId bookmarkId1 = getBookmark(bookmarkDatabase.getBookmarksTree(), 0, 0, 1).getId();
		BookmarkId bookmarkId2 = getBookmark(bookmarkDatabase.getBookmarksTree(), 0, 0, 2).getId();
		BookmarkId bookmarkId3 = getBookmark(bookmarkDatabase.getBookmarksTree(), 0, 0, 3).getId();

		// When
		bookmarkVisited(bookmarkId1);
		Thread.sleep(50);
		bookmarkVisited(bookmarkId1);
		Thread.sleep(50);
		bookmarkVisited(bookmarkId3);
		Thread.sleep(50);
		bookmarkVisited(bookmarkId1);
		Thread.sleep(50);
		bookmarkVisited(bookmarkId2);
		Thread.sleep(50);
		bookmarkVisited(bookmarkId2);

		// Then
		assertThat(visitedBookmarksDatabase.getVisitedBookmarks().getLatestVisitedBookmarks(5)).containsExactly(bookmarkId2,
				bookmarkId1, bookmarkId3);
	}	
	
	@Test
	public void testBookmarkDeleted() throws Exception {
		// Given
		BookmarkId bookmarkId1 = getBookmark(bookmarkDatabase.getBookmarksTree(), 0, 0, 1).getId();
		BookmarkId bookmarkId2 = getBookmark(bookmarkDatabase.getBookmarksTree(), 0, 0, 2).getId();
		BookmarkId bookmarkId3 = getBookmark(bookmarkDatabase.getBookmarksTree(), 0, 0, 3).getId();
		bookmarkVisited(bookmarkId1);
		bookmarkVisited(bookmarkId1);
		bookmarkVisited(bookmarkId3);
		bookmarkVisited(bookmarkId1);
		bookmarkVisited(bookmarkId2);
		bookmarkVisited(bookmarkId2);

		// When
		bookmarkDatabase.modify(bookmarksTreeModifier -> bookmarksTreeModifier.deleteBookmark(bookmarkId1, false));

		// Then
		assertThat(visitedBookmarksDatabase.getVisitedBookmarks().getMostVisitedBookmarks(5)).containsExactly(bookmarkId2,
				bookmarkId3);
	}

	@Test
	public void testBookmarkFolderDeleted() throws Exception {
		// Given
		BookmarkId bookmarkFolderId = getBookmark(bookmarkDatabase.getBookmarksTree(), 0, 0).getId();
		BookmarkId bookmarkId1 = getBookmark(bookmarkDatabase.getBookmarksTree(), 0, 0, 1).getId();
		BookmarkId bookmarkId2 = getBookmark(bookmarkDatabase.getBookmarksTree(), 0, 1).getId();
		BookmarkId bookmarkId3 = getBookmark(bookmarkDatabase.getBookmarksTree(), 0, 2).getId();
		bookmarkVisited(bookmarkId1);
		bookmarkVisited(bookmarkId1);
		bookmarkVisited(bookmarkId3);
		bookmarkVisited(bookmarkId1);
		bookmarkVisited(bookmarkId2);
		bookmarkVisited(bookmarkId2);

		// When
		bookmarkDatabase.modify(bookmarksTreeModifier -> bookmarksTreeModifier.deleteBookmark(bookmarkFolderId, true));

		// Then
		assertThat(visitedBookmarksDatabase.getVisitedBookmarks().getMostVisitedBookmarks(5)).containsExactly(bookmarkId2,
				bookmarkId3);
	}

	@Test
	public void testLoadSaveMostVisitedBookmarks() throws Exception {
		// Given
		BookmarkId bookmarkId1 = getBookmark(bookmarkDatabase.getBookmarksTree(), 0, 0, 1).getId();
		BookmarkId bookmarkId2 = getBookmark(bookmarkDatabase.getBookmarksTree(), 0, 0, 2).getId();
		BookmarkId bookmarkId3 = getBookmark(bookmarkDatabase.getBookmarksTree(), 0, 0, 3).getId();
		bookmarkVisited(bookmarkId1);
		bookmarkVisited(bookmarkId1);
		bookmarkVisited(bookmarkId3);
		bookmarkVisited(bookmarkId1);
		bookmarkVisited(bookmarkId2);
		bookmarkVisited(bookmarkId2);

		// When
		visitedBookmarksDatabase.close();
		visitedBookmarksDatabase = new VisitedBookmarksDatabase(eventBroker, bookmarkDatabase, file);
		visitedBookmarksDatabase.init();

		// Then
		assertThat(visitedBookmarksDatabase.getVisitedBookmarks().getMostVisitedBookmarks(5)).containsExactly(bookmarkId1,
				bookmarkId2, bookmarkId3);
	}

	private void bookmarkVisited(BookmarkId bookmarkId) {
		eventBroker.send(BookmarksEvents.TOPIC_BOOKMARK_VISITED, ImmutableMap.of("bookmarkId", bookmarkId));
	}

}
