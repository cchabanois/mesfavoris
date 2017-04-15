package mesfavoris.internal.numberedbookmarks;

import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmark;
import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmarkFolder;
import static mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder.bookmarksTree;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.eclipse.core.internal.preferences.EclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import mesfavoris.internal.views.virtual.IVirtualBookmarkFolderListener;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder;
import mesfavoris.topics.BookmarksEvents;

public class NumberedBookmarksVirtualFolderTest {
	private NumberedBookmarksVirtualFolder virtualFolder;
	private NumberedBookmarks numberedBookmarks;
	private IEventBroker eventBroker;
	private IEclipsePreferences eclipsePreferences;
	private BookmarkDatabase bookmarkDatabase;

	@Before
	public void setUp() {
		eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);
		eclipsePreferences = new EclipsePreferences();
		numberedBookmarks = new NumberedBookmarks(eclipsePreferences, eventBroker);
		numberedBookmarks.init();
		bookmarkDatabase = new BookmarkDatabase("test", createBookmarksTree());
		virtualFolder = new NumberedBookmarksVirtualFolder(eventBroker, bookmarkDatabase,
				bookmarkDatabase.getBookmarksTree().getRootFolder().getId(), numberedBookmarks);
	}

	@After
	public void tearDown() {
		numberedBookmarks.close();
	}

	@Test
	public void testNoNumberedBookmarks() {
		assertEquals("Numbered bookmarks", virtualFolder.getBookmarkFolder().getPropertyValue(Bookmark.PROPERTY_NAME));
		assertThat(virtualFolder.getChildren()).isEmpty();
		assertThat(virtualFolder.getParentId()).isEqualTo(bookmarkDatabase.getBookmarksTree().getRootFolder().getId());
	}

	@Test
	public void testSomeNumberedBookmarks() {
		// Given
		numberedBookmarks.set(BookmarkNumber.FOUR, new BookmarkId("bookmark11"));
		numberedBookmarks.set(BookmarkNumber.EIGHT, new BookmarkId("folder11"));

		// Then
		assertThat(virtualFolder.getChildren())
				.extracting(bookmarkLink -> bookmarkLink.getBookmark().getPropertyValue(Bookmark.PROPERTY_NAME))
				.containsExactly("bookmark11", "folder11");

	}
	
	@Test
	public void testListener() {
		// Given
		IVirtualBookmarkFolderListener listener = mock(IVirtualBookmarkFolderListener.class);
		virtualFolder.addListener(listener);
		
		// When
		eventBroker.send(BookmarksEvents.TOPIC_NUMBERED_BOOKMARKS_CHANGED, null);
		
		// Then
		verify(listener).childrenChanged(virtualFolder);
	}

	private BookmarksTree createBookmarksTree() {
		BookmarksTreeBuilder bookmarksTreeBuilder = bookmarksTree("root");
		bookmarksTreeBuilder.addBookmarks("root", bookmarkFolder("folder1"), bookmarkFolder("folder2"));
		bookmarksTreeBuilder.addBookmarks("folder1", bookmarkFolder("folder11"), bookmark("bookmark11"));

		return bookmarksTreeBuilder.build();
	}

}
