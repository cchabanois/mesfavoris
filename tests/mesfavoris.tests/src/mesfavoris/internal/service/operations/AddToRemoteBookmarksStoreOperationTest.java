package mesfavoris.internal.service.operations;

import static mesfavoris.MesFavoris.DEFAULT_BOOKMARKFOLDER_ID;
import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmark;
import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmarkFolder;
import static mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder.bookmarksTree;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import mesfavoris.BookmarksException;
import mesfavoris.internal.remote.InMemoryRemoteBookmarksStore;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.remote.RemoteBookmarkFolder;
import mesfavoris.remote.RemoteBookmarksStoreManager;
import mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder;

public class AddToRemoteBookmarksStoreOperationTest {
	private AddToRemoteBookmarksStoreOperation operation;
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
		this.operation = new AddToRemoteBookmarksStoreOperation(bookmarkDatabase, remoteBookmarksStoreManager);
	}

	@Test
	public void testAddToRemoteBookmarksStore() throws Exception {
		// Given
		String storeId = remoteBookmarksStore.getDescriptor().getId();
		BookmarkId bookmarkFolderId = new BookmarkId("folder1");
		remoteBookmarksStore.connect(new NullProgressMonitor());

		// When
		operation.addToRemoteBookmarksStore(storeId, bookmarkFolderId, new NullProgressMonitor());

		// Then
		Optional<RemoteBookmarkFolder> remoteBookmarkFolder = remoteBookmarksStore
				.getRemoteBookmarkFolder(bookmarkFolderId);
		assertTrue(remoteBookmarkFolder.isPresent());
	}

	@Test
	public void testCannotAddDefaultFolderToRemoteBookmarksStore() throws Exception {
		// Given
		String storeId = remoteBookmarksStore.getDescriptor().getId();
		remoteBookmarksStore.connect(new NullProgressMonitor());

		// When
		assertThatThrownBy(() -> {
			operation.addToRemoteBookmarksStore(storeId, DEFAULT_BOOKMARKFOLDER_ID,
					new NullProgressMonitor());
		}).isInstanceOf(BookmarksException.class).withFailMessage("Could not add bookmark folder to remote store");
	}

	@Test
	public void testCannotAddFolderIfUnderRemoteBookmarkFolder() throws Exception {
		// Given
		String storeId = remoteBookmarksStore.getDescriptor().getId();
		remoteBookmarksStore.connect(new NullProgressMonitor());
		operation.addToRemoteBookmarksStore(storeId, new BookmarkId("folder1"), new NullProgressMonitor());
		
		// When
		assertThatThrownBy(() -> {
			operation.addToRemoteBookmarksStore(storeId, new BookmarkId("folder11"),
					new NullProgressMonitor());
		}).isInstanceOf(BookmarksException.class).withFailMessage("Could not add bookmark folder to remote store");
	}

	@Test
	public void testCannotAddToRemoteBookmarksStoreIfNotConnected() throws BookmarksException {
		String storeId = remoteBookmarksStore.getDescriptor().getId();
		BookmarkId bookmarkFolderId = new BookmarkId("folder1");

		// When/Then
		assertThatThrownBy(() -> {
			operation.addToRemoteBookmarksStore(storeId, bookmarkFolderId, new NullProgressMonitor());
		}).isInstanceOf(BookmarksException.class).withFailMessage("Could not add bookmark folder to remote store");
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
