package mesfavoris.internal.persistence;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.chabanois.mesfavoris.internal.persistence.RemoteBookmarksSaver;
import org.chabanois.mesfavoris.model.Bookmark;
import org.chabanois.mesfavoris.model.BookmarkFolder;
import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.model.BookmarksTree;
import org.chabanois.mesfavoris.model.modification.BookmarksTreeModifier;
import org.chabanois.mesfavoris.remote.RemoteBookmarksStoreManager;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import mesfavoris.internal.remote.InMemoryRemoteBookmarksStore;
import mesfavoris.testutils.BookmarksTreeBuilder;
import mesfavoris.testutils.IncrementalIDGenerator;
import mesfavoris.testutils.RandomModificationApplier;

public class RemoteBookmarksSaverTest {
	private RemoteBookmarksStoreManager remoteBookmarksStoreManager;
	private InMemoryRemoteBookmarksStore remoteBookmarksStore;
	private RemoteBookmarksSaver saver;
	private BookmarksTree originalBookmarksTree;
	private IncrementalIDGenerator incrementalIDGenerator = new IncrementalIDGenerator();

	@Before
	public void setUp() throws IOException {
		IEventBroker eventBroker = mock(IEventBroker.class);
		this.remoteBookmarksStore = new InMemoryRemoteBookmarksStore(eventBroker);
		remoteBookmarksStoreManager = new RemoteBookmarksStoreManager(()->Lists.newArrayList(remoteBookmarksStore));
		saver = new RemoteBookmarksSaver(remoteBookmarksStoreManager);
		originalBookmarksTree = new BookmarksTreeBuilder(incrementalIDGenerator, 5, 3, 2).build();
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
		RandomModificationApplier randomModificationApplier = new RandomModificationApplier(incrementalIDGenerator);
		randomModificationApplier.applyRandomModification(bookmarksTreeModifier, new PrintWriter(new StringWriter()));

	}

}
