package mesfavoris.internal.recent;

import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.*;
import static mesfavoris.tests.commons.bookmarks.BookmarksTreeTestUtil.getBookmark;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.assertj.core.util.Lists;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import mesfavoris.BookmarksException;
import mesfavoris.internal.visited.VisitedBookmarksDatabase;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;

public class RecentBookmarksDatabaseTest {
	private RecentBookmarksDatabase recentBookmarksDatabase;
	private BookmarkDatabase bookmarkDatabase;
	private File file;
	private IEventBroker eventBroker = mock(IEventBroker.class);
	private final BookmarkId rootBookmarkId = new BookmarkId("root");
	private final Duration recentDuration = Duration.ofDays(1);
	
	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Before
	public void setUp() throws Exception {
		BookmarksTree bookmarksTree = new BookmarksTree(new BookmarkFolder(rootBookmarkId, "root"));
		bookmarkDatabase = new BookmarkDatabase("main", bookmarksTree);
		file = temporaryFolder.newFile();
		recentBookmarksDatabase = new RecentBookmarksDatabase(eventBroker, bookmarkDatabase, file, recentDuration);
		recentBookmarksDatabase.init();
	}

	@After
	public void tearDown() throws Exception {
		recentBookmarksDatabase.close();
	}

	@Test
	public void testAddBookmark() throws Exception {
		// Given
		addBookmark(rootBookmarkId, "bookmark1", Instant.now());
		addBookmark(rootBookmarkId, "bookmark2", Instant.now());
		addBookmark(rootBookmarkId, bookmark("bookmark3").build());

		// When
		List<BookmarkId> recentBookmarks = recentBookmarksDatabase.getRecentBookmarks().getRecentBookmarks(10);

		// Then
		assertThat(recentBookmarks).containsExactly(new BookmarkId("bookmark2"), new BookmarkId("bookmark1"));
	}

	@Test
	public void testDeleteBookmark() throws Exception {
		// Given
		addBookmark(rootBookmarkId, "bookmark1", Instant.now());
		addBookmark(rootBookmarkId, "bookmark2", Instant.now());
		addBookmark(rootBookmarkId, "bookmark3", Instant.now());

		// When
		deleteBookmark(new BookmarkId("bookmark2"));
		List<BookmarkId> recentBookmarks = recentBookmarksDatabase.getRecentBookmarks().getRecentBookmarks(10);

		// Then
		assertThat(recentBookmarks).containsExactly(new BookmarkId("bookmark3"), new BookmarkId("bookmark1"));
	}

	@Test
	public void testDeleteBookmarkFolder() throws Exception {
		// Given
		addBookmark(rootBookmarkId, "bookmark1", Instant.now());
		BookmarkId bookmarkFolderId = new BookmarkId("bookmarkFolder");
		addBookmarkFolder(rootBookmarkId, "bookmarkFolder", Instant.now());
		addBookmark(bookmarkFolderId, "bookmark2", Instant.now());
		addBookmark(bookmarkFolderId, "bookmark3", Instant.now());

		// When
		deleteBookmark(new BookmarkId("bookmarkFolder"));
		List<BookmarkId> recentBookmarks = recentBookmarksDatabase.getRecentBookmarks().getRecentBookmarks(10);

		// Then
		assertThat(recentBookmarks).containsExactly(new BookmarkId("bookmark1"));
	}

	@Test
	public void testOnlyKeepRecentBookmarks() throws BookmarksException, InterruptedException {
		// Given
		addBookmark(rootBookmarkId, "bookmark1", Instant.now().minus(recentDuration).plus(Duration.ofSeconds(1)));
		assertThat(recentBookmarksDatabase.getRecentBookmarks().getRecentBookmarks(10))
				.containsExactly(new BookmarkId("bookmark1"));

		// When
		Thread.sleep(2000);
		addBookmark(rootBookmarkId, "bookmark2", Instant.now());
		List<BookmarkId> recentBookmarks = recentBookmarksDatabase.getRecentBookmarks().getRecentBookmarks(10);

		// Then
		assertThat(recentBookmarks).containsExactly(new BookmarkId("bookmark2"));
	}

	@Test
	public void testLoadSaveRecentBookmarks() throws Exception {
		// Given
		addBookmark(rootBookmarkId, "bookmark1", Instant.now());
		addBookmark(rootBookmarkId, "bookmark2", Instant.now());
		addBookmark(rootBookmarkId, "bookmark3", Instant.now());

		// When
		Thread.sleep(2000); // should not be necessary but test fails on assertThat on travis too often
		recentBookmarksDatabase.close();
		recentBookmarksDatabase = new RecentBookmarksDatabase(eventBroker, bookmarkDatabase, file, recentDuration);
		recentBookmarksDatabase.init();

		// Then
		List<BookmarkId> recentBookmarks = recentBookmarksDatabase.getRecentBookmarks().getRecentBookmarks(10);
		assertThat(recentBookmarks).containsExactly(new BookmarkId("bookmark3"), new BookmarkId("bookmark2"), new BookmarkId("bookmark1"));
	}	
	
	private void addBookmarkFolder(BookmarkId parentId, String name, Instant created) throws BookmarksException {
		BookmarkFolder bookmarkFolder = bookmarkFolder(name).created(created).build();
		addBookmark(parentId, bookmarkFolder);
	}

	private void addBookmark(BookmarkId parentId, String name, Instant created) throws BookmarksException {
		Bookmark bookmark = bookmark(name).created(created).build();
		addBookmark(parentId, bookmark);
	}

	private void addBookmark(BookmarkId parentId, Bookmark bookmark) throws BookmarksException {
		bookmarkDatabase.modify(bookmarksTreeModifier -> {
			bookmarksTreeModifier.addBookmarks(parentId, Lists.newArrayList(bookmark));
		});
	}

	private void deleteBookmark(BookmarkId bookmarkId) throws BookmarksException {
		bookmarkDatabase.modify(bookmarksTreeModifier -> {
			bookmarksTreeModifier.deleteBookmark(bookmarkId, true);
		});
	}
}
