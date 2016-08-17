package mesfavoris.internal.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Predicate;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.Lists;

import mesfavoris.BookmarksException;
import mesfavoris.internal.remote.InMemoryRemoteBookmarksStore;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.model.modification.BookmarksTreeModifier;
import mesfavoris.remote.RemoteBookmarksStoreManager;
import mesfavoris.testutils.BookmarksTreeGenerator;
import mesfavoris.testutils.IncrementalIDGenerator;
import mesfavoris.testutils.RandomModificationApplier;

public class RemoteBookmarksSaverTest {
	private RemoteBookmarksStoreManager remoteBookmarksStoreManager;
	private InMemoryRemoteBookmarksStore remoteBookmarksStore;
	private RemoteBookmarksSaver saver;
	private BookmarksTree originalBookmarksTree;
	private IncrementalIDGenerator incrementalIDGenerator = new IncrementalIDGenerator();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws IOException {
		IEventBroker eventBroker = mock(IEventBroker.class);
		this.remoteBookmarksStore = new InMemoryRemoteBookmarksStore(eventBroker);
		remoteBookmarksStoreManager = new RemoteBookmarksStoreManager(() -> Lists.newArrayList(remoteBookmarksStore));
		saver = new RemoteBookmarksSaver(remoteBookmarksStoreManager);
		originalBookmarksTree = new BookmarksTreeGenerator(incrementalIDGenerator, 5, 3, 2).build();
		addAllTopLevelBookmarkFoldersToRemoteBookmarksStore();
	}

	private void addAllTopLevelBookmarkFoldersToRemoteBookmarksStore() throws IOException {
		for (Bookmark bookmark : originalBookmarksTree.getChildren(originalBookmarksTree.getRootFolder().getId())) {
			if (bookmark instanceof BookmarkFolder) {
				remoteBookmarksStore.add(originalBookmarksTree, bookmark.getId(), new NullProgressMonitor());
			}
		}
	}

	@Test
	public void testMoveRemoteBookmarkFolder() throws BookmarksException, IOException {
		// Given
		BookmarksTreeModifier bookmarksTreeModifier = new BookmarksTreeModifier(originalBookmarksTree);
		BookmarkId rootId = bookmarksTreeModifier.getCurrentTree().getRootFolder().getId();
		bookmarksTreeModifier.moveBefore(
				Lists.newArrayList(bookmarksTreeModifier.getCurrentTree().getChildren(rootId).get(2).getId()), rootId,
				bookmarksTreeModifier.getCurrentTree().getChildren(rootId).get(0).getId());

		// When
		boolean saved = saver.applyModificationsToRemoteBookmarksStores(bookmarksTreeModifier.getModifications(),
				new NullProgressMonitor());

		// Then
		assertFalse(saved);
		verify(bookmarksTreeModifier.getCurrentTree());
	}

	@Test
	public void testApplyRandomModifications() throws Exception {
		// Given
		BookmarksTreeModifier bookmarksTreeModifier = new BookmarksTreeModifier(originalBookmarksTree);
		randomModifications(bookmarksTreeModifier, 30);

		// When
		saver.applyModificationsToRemoteBookmarksStores(bookmarksTreeModifier.getModifications(),
				new NullProgressMonitor());

		// Then
		verify(bookmarksTreeModifier.getCurrentTree());
	}

	private void verify(BookmarksTree bookmarksTree) throws IOException {
		for (BookmarkId bookmarkFolderId : remoteBookmarksStore.getRemoteBookmarkFolderIds()) {
			assertEquals(bookmarksTree.subTree(bookmarkFolderId).toString(), remoteBookmarksStore
					.load(bookmarkFolderId, new NullProgressMonitor()).getBookmarksTree().toString());
		}
	}

	private void randomModifications(BookmarksTreeModifier bookmarksTreeModifier, int n) {
		for (int i = 0; i < n; i++) {
			randomModification(bookmarksTreeModifier);
		}
	}

	private void randomModification(BookmarksTreeModifier bookmarksTreeModifier) {
		Predicate<Bookmark> onlyUnderRemoteBookmarkFolder = bookmark -> remoteBookmarksStoreManager
				.getRemoteBookmarkFolder(bookmark.getId()) == null
				&& remoteBookmarksStoreManager.getRemoteBookmarkFolderContaining(bookmarksTreeModifier.getCurrentTree(),
						bookmark.getId()) != null;
		RandomModificationApplier randomModificationApplier = new RandomModificationApplier(incrementalIDGenerator,
				onlyUnderRemoteBookmarkFolder);
		randomModificationApplier.applyRandomModification(bookmarksTreeModifier, new PrintWriter(new StringWriter()));

	}

}
