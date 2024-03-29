package mesfavoris.internal.service.operations;

import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmark;
import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmarkFolder;
import static mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder.bookmarksTree;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import mesfavoris.internal.remote.InMemoryRemoteBookmarksStore;
import mesfavoris.internal.service.operations.RefreshRemoteFolderOperation;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.persistence.IBookmarksDirtyStateTracker;
import mesfavoris.remote.RemoteBookmarksStoreManager;
import mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder;

public class RefreshRemoteFolderOperationTest {
	private InMemoryRemoteBookmarksStore remoteBookmarksStore;
	private RemoteBookmarksStoreManager remoteBookmarksStoreManager;
	private RefreshRemoteFolderOperation refreshRemoteFolderOperation;
	private IBookmarksDirtyStateTracker bookmarksDirtyStateTracker = mock(IBookmarksDirtyStateTracker.class);
	private BookmarkDatabase bookmarkDatabase;

	@Before
	public void setUp() {
		IEventBroker eventBroker = mock(IEventBroker.class);
		this.remoteBookmarksStore = new InMemoryRemoteBookmarksStore(eventBroker);
		this.remoteBookmarksStoreManager = new RemoteBookmarksStoreManager(
				() -> Lists.newArrayList(remoteBookmarksStore));
		this.bookmarkDatabase = new BookmarkDatabase("testId", getInitialTree());
		this.refreshRemoteFolderOperation = new RefreshRemoteFolderOperation(bookmarkDatabase,
				remoteBookmarksStoreManager, bookmarksDirtyStateTracker);
	}

	@Test
	public void testRefreshRemoteFolder() throws Exception {
		// Given
		remoteBookmarksStore.add(getRemoteBookmarkFolder2(), new BookmarkId("bookmarkFolder2"),
				new NullProgressMonitor());

		// When
		refreshRemoteFolderOperation.refresh(new BookmarkId("bookmarkFolder2"), new NullProgressMonitor());

		// Then
		assertEquals(getRemoteBookmarkFolder2().toString(),
				bookmarkDatabase.getBookmarksTree().subTree(new BookmarkId("bookmarkFolder2")).toString());
	}

	@Test
	public void testRefreshAllRemoteFolders() throws Exception {
		// Given
		remoteBookmarksStore.add(getRemoteBookmarkFolder2(), new BookmarkId("bookmarkFolder2"),
				new NullProgressMonitor());
		remoteBookmarksStore.connect(new NullProgressMonitor());

		// When
		refreshRemoteFolderOperation.refresh(new NullProgressMonitor());

		// Then
		assertEquals(getRemoteBookmarkFolder2().toString(),
				bookmarkDatabase.getBookmarksTree().subTree(new BookmarkId("bookmarkFolder2")).toString());
	}	
	
	@Test
	public void testRefreshRemoteFolderWaitsUntilNotDirty() throws Exception {
		// Given
		remoteBookmarksStore.add(getRemoteBookmarkFolder2(), new BookmarkId("bookmarkFolder2"),
				new NullProgressMonitor());
		when(bookmarksDirtyStateTracker.isDirty()).thenReturn(true, true, true, false);

		// When
		refreshRemoteFolderOperation.refresh(new BookmarkId("bookmarkFolder2"), new NullProgressMonitor());

		// Then
		assertEquals(getRemoteBookmarkFolder2().toString(),
				bookmarkDatabase.getBookmarksTree().subTree(new BookmarkId("bookmarkFolder2")).toString());
		verify(bookmarksDirtyStateTracker, times(4)).isDirty();

	}

	private BookmarksTree getInitialTree() {
		BookmarksTreeBuilder bookmarksTreeBuilder = bookmarksTree("rootFolder");
		bookmarksTreeBuilder.addBookmarks("rootFolder", bookmarkFolder("bookmarkFolder1"),
				bookmarkFolder("bookmarkFolder2"));
		bookmarksTreeBuilder.addBookmarks("bookmarkFolder1", bookmark("bookmark1"), bookmark("bookmark2"));
		bookmarksTreeBuilder.addBookmarks("bookmarkFolder2", bookmark("bookmark3"), bookmark("bookmark4"));
		return bookmarksTreeBuilder.build();
	}

	private BookmarksTree getRemoteBookmarkFolder2() {
		BookmarksTreeBuilder bookmarksTreeBuilder = bookmarksTree("bookmarkFolder2");
		bookmarksTreeBuilder.addBookmarks("bookmarkFolder2", bookmark("bookmark3"), bookmark("bookmark4"),
				bookmark("bookmark5"));
		return bookmarksTreeBuilder.build();
	}

}
