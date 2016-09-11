package mesfavoris.internal.persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder;
import mesfavoris.tests.commons.waits.Waiter;

import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmark;
import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmarkFolder;
import static mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder.bookmarksTree;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import org.eclipse.core.runtime.IProgressMonitor;

public class BookmarksAutoSaverTest {
	private BookmarksAutoSaver bookmarksAutoSaver;
	private LocalBookmarksSaver localBookmarksSaver = mock(LocalBookmarksSaver.class);
	private RemoteBookmarksSaver remoteBookmarksSaver = mock(RemoteBookmarksSaver.class);
	private BookmarkDatabase bookmarkDatabase;

	@Before
	public void setUp() {
		bookmarkDatabase = new BookmarkDatabase("test", getInitialTree());
		bookmarksAutoSaver = new BookmarksAutoSaver(bookmarkDatabase, localBookmarksSaver, remoteBookmarksSaver);
		bookmarksAutoSaver.init();
	}

	@After
	public void tearDown() {
		bookmarksAutoSaver.close();
	}

	@Test
	public void testAutoSave() throws Exception {
		// When
		bookmarkDatabase.modify(bookmarksTreeModifier -> bookmarksTreeModifier
				.setPropertyValue(new BookmarkId("bookmark1"), Bookmark.PROPERTY_NAME, "bookmark1 renamed"));
		Waiter.waitUntil("bookmarks database dirty", () -> !bookmarksAutoSaver.isDirty());

		// Then
		verify(localBookmarksSaver).saveBookmarks(any(BookmarksTree.class), any(IProgressMonitor.class));
		verify(remoteBookmarksSaver).applyModificationsToRemoteBookmarksStores(anyList(), any(IProgressMonitor.class));
	}

	@Test
	public void testDirtyBookmarks() throws Exception {
		// When
		bookmarkDatabase.modify(bookmarksTreeModifier -> bookmarksTreeModifier
				.setPropertyValue(new BookmarkId("bookmark1"), Bookmark.PROPERTY_NAME, "bookmark1 renamed"));

		// Then
		assertTrue(bookmarksAutoSaver.isDirty());
		assertThat(bookmarksAutoSaver.getDirtyBookmarks()).containsExactly(new BookmarkId("bookmark1"));
	}

	private BookmarksTree getInitialTree() {
		BookmarksTreeBuilder bookmarksTreeBuilder = bookmarksTree("rootFolder");
		bookmarksTreeBuilder.addBookmarks("rootFolder", bookmarkFolder("bookmarkFolder1"),
				bookmarkFolder("bookmarkFolder2"));
		bookmarksTreeBuilder.addBookmarks("bookmarkFolder1", bookmark("bookmark1"), bookmark("bookmark2"));
		bookmarksTreeBuilder.addBookmarks("bookmarkFolder2", bookmark("bookmark3"), bookmark("bookmark4"));
		return bookmarksTreeBuilder.build();
	}

}
