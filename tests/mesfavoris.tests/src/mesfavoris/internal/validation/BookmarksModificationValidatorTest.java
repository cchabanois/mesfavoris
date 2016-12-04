package mesfavoris.internal.validation;

import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmark;
import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmarkFolder;
import static mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder.bookmarksTree;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import mesfavoris.internal.remote.InMemoryRemoteBookmarksStore;
import mesfavoris.internal.workspace.DefaultBookmarkFolderProvider;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.model.modification.BookmarksModification;
import mesfavoris.model.modification.BookmarksTreeModifier;
import mesfavoris.remote.RemoteBookmarkFolder;
import mesfavoris.remote.RemoteBookmarksStoreManager;
import mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder;

public class BookmarksModificationValidatorTest {
	private InMemoryRemoteBookmarksStore remoteBookmarksStore;
	private BookmarksModificationValidator bookmarksModificationValidator;
	private RemoteBookmarksStoreManager remoteBookmarksStoreManager;
	private BookmarksTreeModifier bookmarksTreeModifier;
	private BookmarksTree bookmarksTree;

	@Before
	public void setUp() {
		IEventBroker eventBroker = mock(IEventBroker.class);
		this.bookmarksTree = createBookmarksTree();
		this.remoteBookmarksStore = new InMemoryRemoteBookmarksStore(eventBroker);
		this.remoteBookmarksStoreManager = new RemoteBookmarksStoreManager(
				() -> Lists.newArrayList(remoteBookmarksStore));
		bookmarksModificationValidator = new BookmarksModificationValidator(remoteBookmarksStoreManager);
		this.bookmarksTreeModifier = new BookmarksTreeModifier(bookmarksTree);
	}

	@Test
	public void testCannotAddBookmarkInsideRemoteBookmarkFolderIfNotConnected() throws Exception {
		// Given
		addToRemoteBookmarksStore(new BookmarkId("folder1"));

		// When
		bookmarksTreeModifier.addBookmarks(new BookmarkId("folder11"),
				Lists.newArrayList(bookmark("bookmark111").build()));

		// Then
		IStatus status = validateModifications();
		assertFalse(status.isOK());
	}

	@Test
	public void testCanAddBookmarkInsideRemoteBookmarkFolderIfConnected() throws Exception {
		// Given
		addToRemoteBookmarksStore(new BookmarkId("folder1"));
		remoteBookmarksStore.connect(new NullProgressMonitor());

		// When
		bookmarksTreeModifier.addBookmarks(new BookmarkId("folder11"),
				Lists.newArrayList(bookmark("bookmark111").build()));

		// Then
		IStatus status = validateModifications();
		assertTrue(status.isOK());
	}

	@Test
	public void testCannotAddBookmarkInsideRemoteBookmarkFolderIfReadOnly() throws Exception {
		// Given
		addToRemoteBookmarksStore(new BookmarkId("folder1"));
		remoteBookmarksStore.addRemoteBookmarkFolderProperty(new BookmarkId("folder1"),
				RemoteBookmarkFolder.PROP_READONLY, Boolean.TRUE.toString());
		remoteBookmarksStore.connect(new NullProgressMonitor());

		// When
		bookmarksTreeModifier.addBookmarks(new BookmarkId("folder11"),
				Lists.newArrayList(bookmark("bookmark111").build()));

		// Then
		IStatus status = validateModifications();
		assertFalse(status.isOK());
	}

	@Test
	public void testCannotMoveRemoteBookmarkFolderInsideAnotherRemoteBookmarkFolder() throws IOException {
		// Given
		addToRemoteBookmarksStore(new BookmarkId("folder1"));
		addToRemoteBookmarksStore(new BookmarkId("folder21"));
		remoteBookmarksStore.connect(new NullProgressMonitor());

		// When
		bookmarksTreeModifier.move(Lists.newArrayList(new BookmarkId("folder2")), new BookmarkId("folder11"));

		// Then
		IStatus status = validateModifications();
		assertFalse(status.isOK());
	}

	private IStatus validateModifications() {
		for (BookmarksModification bookmarksModification : bookmarksTreeModifier.getModifications()) {
			IStatus status = bookmarksModificationValidator.validateModification(bookmarksModification);
			if (!status.isOK()) {
				return status;
			}
		}
		return Status.OK_STATUS;
	}

	private void addToRemoteBookmarksStore(BookmarkId bookmarkFolderId) throws IOException {
		remoteBookmarksStore.add(bookmarksTree.subTree(bookmarkFolderId), bookmarkFolderId, new NullProgressMonitor());
	}

	private BookmarksTree createBookmarksTree() {
		BookmarksTreeBuilder bookmarksTreeBuilder = bookmarksTree("root");
		bookmarksTreeBuilder.addBookmarks("root", bookmarkFolder("folder1"), bookmarkFolder("folder2"),
				bookmarkFolder(DefaultBookmarkFolderProvider.DEFAULT_BOOKMARKFOLDER_ID, "default"));
		bookmarksTreeBuilder.addBookmarks("folder1", bookmarkFolder("folder11"), bookmark("bookmark11"),
				bookmark("bookmark12"));
		bookmarksTreeBuilder.addBookmarks("folder2", bookmarkFolder("folder21"), bookmark("bookmark21"),
				bookmark("bookmark22"));

		return bookmarksTreeBuilder.build();
	}

}
