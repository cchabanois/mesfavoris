package mesfavoris.internal.service.operations;

import static mesfavoris.MesFavoris.DEFAULT_BOOKMARKFOLDER_ID;
import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmark;
import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmarkFolder;
import static mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder.bookmarksTree;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import mesfavoris.internal.remote.InMemoryRemoteBookmarksStore;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.persistence.IBookmarksDirtyStateTracker;
import mesfavoris.remote.IRemoteBookmarksStore.State;
import mesfavoris.remote.RemoteBookmarksStoreManager;
import mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder;

public class ConnectToRemoteBookmarkStoreOperationTest {
	private ConnectToRemoteBookmarksStoreOperation operation;
	private BookmarkDatabase bookmarkDatabase;
	private InMemoryRemoteBookmarksStore remoteBookmarksStore;
	private RemoteBookmarksStoreManager remoteBookmarksStoreManager;

	@Before
	public void setUp() {
		IEventBroker eventBroker = mock(IEventBroker.class);
		this.bookmarkDatabase = new BookmarkDatabase("test", createBookmarksTree());
		this.remoteBookmarksStore = new InMemoryRemoteBookmarksStore(eventBroker);
		this.remoteBookmarksStoreManager = new RemoteBookmarksStoreManager(
				() -> Lists.newArrayList(remoteBookmarksStore));
		IBookmarksDirtyStateTracker bookmarksDirtyStateTracker = mock(IBookmarksDirtyStateTracker.class);
		when(bookmarksDirtyStateTracker.isDirty()).thenReturn(false);
		this.operation = new ConnectToRemoteBookmarksStoreOperation(bookmarkDatabase, remoteBookmarksStoreManager,
				bookmarksDirtyStateTracker);
	}

	@Test
	public void testConnect() throws Exception {
		// Given
		BookmarksTree remoteBookmarksTree = bookmarksTree("folder1")
				.addBookmarks("folder1", bookmarkFolder("folder11"), bookmark("bookmark11"),
						bookmark("bookmark12").withProperty(Bookmark.PROPERTY_COMMENT, "comment for bookmark12")
								.withProperty("customProperty", "modified custom value"))
				.build();
		remoteBookmarksStore.add(remoteBookmarksTree, new BookmarkId("folder1"), new NullProgressMonitor());

		// When
		operation.connect(remoteBookmarksStore.getDescriptor().getId(), new NullProgressMonitor());

		// Then
		assertEquals(State.connected, remoteBookmarksStore.getState());
		assertEquals("modified custom value", bookmarkDatabase.getBookmarksTree()
				.getBookmark(new BookmarkId("bookmark12")).getPropertyValue("customProperty"));
	}

	@Test
	public void testDisconnect() throws Exception {
		// Given
		BookmarksTree remoteBookmarksTree = bookmarksTree("folder1")
				.addBookmarks("folder1", bookmarkFolder("folder11"), bookmark("bookmark11"),
						bookmark("bookmark12").withProperty(Bookmark.PROPERTY_COMMENT, "comment for bookmark12")
								.withProperty("customProperty", "modified custom value"))
				.build();
		remoteBookmarksStore.add(remoteBookmarksTree, new BookmarkId("folder1"), new NullProgressMonitor());
		operation.connect(remoteBookmarksStore.getDescriptor().getId(), new NullProgressMonitor());

		// When
		operation.disconnect(remoteBookmarksStore.getDescriptor().getId(), new NullProgressMonitor());

		// Then
		assertEquals(State.disconnected, remoteBookmarksStore.getState());
		assertEquals("modified custom value", bookmarkDatabase.getBookmarksTree()
				.getBookmark(new BookmarkId("bookmark12")).getPropertyValue("customProperty"));
	}

	private BookmarksTree createBookmarksTree() {
		BookmarksTreeBuilder bookmarksTreeBuilder = bookmarksTree("root");
		bookmarksTreeBuilder.addBookmarks("root", bookmarkFolder("folder1"), bookmarkFolder("folder2"),
				bookmarkFolder(DEFAULT_BOOKMARKFOLDER_ID, "default"));
		bookmarksTreeBuilder.addBookmarks("folder1", bookmarkFolder("folder11"), bookmark("bookmark11"),
				bookmark("bookmark12").withProperty(Bookmark.PROPERTY_COMMENT, "comment for bookmark12")
						.withProperty("customProperty", "custom value"));

		return bookmarksTreeBuilder.build();
	}

}
