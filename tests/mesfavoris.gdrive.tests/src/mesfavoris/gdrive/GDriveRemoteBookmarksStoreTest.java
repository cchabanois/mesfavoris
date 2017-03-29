package mesfavoris.gdrive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.time.Duration;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import mesfavoris.gdrive.GDriveRemoteBookmarksStore;
import mesfavoris.gdrive.changes.BookmarksFileChangeManager;
import mesfavoris.gdrive.mappings.BookmarkMappingsPersister;
import mesfavoris.gdrive.mappings.BookmarkMappingsStore;
import mesfavoris.gdrive.test.GDriveConnectionRule;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.remote.AbstractRemoteBookmarksStore;
import mesfavoris.remote.ConflictException;
import mesfavoris.remote.IRemoteBookmarksStoreDescriptor;
import mesfavoris.remote.RemoteBookmarksTree;
import mesfavoris.remote.IRemoteBookmarksStore.State;

import static org.mockito.Mockito.*;

public class GDriveRemoteBookmarksStoreTest {
	private static final String ID = "gDrive";
	private GDriveRemoteBookmarksStore gDriveRemoteBookmarksStore;
	private IEventBroker eventBroker = mock(IEventBroker.class);
	private BookmarkMappingsStore bookmarkMappingsStore;
	private IRemoteBookmarksStoreDescriptor remoteBookmarksStoreDescriptor = mock(
			IRemoteBookmarksStoreDescriptor.class);
	private BookmarksFileChangeManager bookmarksFileChangeManager;

	@Rule
	public GDriveConnectionRule gDriveConnectionRule = new GDriveConnectionRule(GDriveTestUser.USER1, false);

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Before
	public void setUp() throws Exception {
		bookmarkMappingsStore = new BookmarkMappingsStore(new BookmarkMappingsPersister(temporaryFolder.newFile()));
		bookmarksFileChangeManager = new BookmarksFileChangeManager(gDriveConnectionRule.getGDriveConnectionManager(),
				bookmarkMappingsStore, ()->Duration.ofSeconds(5));
		gDriveRemoteBookmarksStore = new GDriveRemoteBookmarksStore(eventBroker,
				gDriveConnectionRule.getGDriveConnectionManager(), bookmarkMappingsStore, bookmarksFileChangeManager);
		when(remoteBookmarksStoreDescriptor.getId()).thenReturn(ID);
		gDriveRemoteBookmarksStore.init(remoteBookmarksStoreDescriptor);
	}

	@After
	public void tearDown() throws IOException {
		if (gDriveRemoteBookmarksStore.getState() == State.connected) {
			disconnect();
		}
	}

	@Test
	public void testConnect() throws IOException {
		// Given

		connect();

		// Then
		assertEquals(State.connected, gDriveRemoteBookmarksStore.getState());
		verify(eventBroker).post(AbstractRemoteBookmarksStore.getConnectedTopic(ID), true);
	}

	@Test
	public void testDisconnect() throws IOException {
		// Given
		connect();

		// When
		disconnect();

		// Then
		assertEquals(State.disconnected, gDriveRemoteBookmarksStore.getState());
		verify(eventBroker).post(AbstractRemoteBookmarksStore.getConnectedTopic(ID), false);
	}

	@Test
	public void testAdd() throws IOException {
		// Given
		BookmarksTree bookmarksTree = new BookmarksTree(new BookmarkFolder(new BookmarkId("tree1"), Maps.newHashMap()));
		connect();

		// When
		gDriveRemoteBookmarksStore.add(bookmarksTree, bookmarksTree.getRootFolder().getId(), new NullProgressMonitor());

		// Then
		RemoteBookmarksTree remoteBookmarksTree = gDriveRemoteBookmarksStore.load(bookmarksTree.getRootFolder().getId(),
				new NullProgressMonitor());
		assertEquals(bookmarksTree.toString(), remoteBookmarksTree.getBookmarksTree().toString());
		assertTrue(
				gDriveRemoteBookmarksStore.getRemoteBookmarkFolder(bookmarksTree.getRootFolder().getId()).isPresent());
	}

	@Test
	public void testRemove() throws IOException {
		// Given
		BookmarksTree bookmarksTree = new BookmarksTree(new BookmarkFolder(new BookmarkId("tree1"), Maps.newHashMap()));
		connect();
		gDriveRemoteBookmarksStore.add(bookmarksTree, bookmarksTree.getRootFolder().getId(), new NullProgressMonitor());

		// When
		gDriveRemoteBookmarksStore.remove(bookmarksTree.getRootFolder().getId(), new NullProgressMonitor());

		// Then
		assertEquals(Sets.newHashSet(), gDriveRemoteBookmarksStore.getRemoteBookmarkFolders());
	}

	@Test
	public void testSave() throws Exception {
		// Given
		BookmarksTree bookmarksTree = new BookmarksTree(new BookmarkFolder(new BookmarkId("tree1"), Maps.newHashMap()));
		connect();
		RemoteBookmarksTree remoteBookmarksTree = gDriveRemoteBookmarksStore.add(bookmarksTree,
				bookmarksTree.getRootFolder().getId(), new NullProgressMonitor());

		// When
		bookmarksTree = bookmarksTree.setPropertyValue(bookmarksTree.getRootFolder().getId(), "myProperty",
				"myPropertyValue");
		// do not set etag. Otherwise it sometimes fails. Looks like the file is "modified" by gdrive after creation.
		gDriveRemoteBookmarksStore.save(bookmarksTree, bookmarksTree.getRootFolder().getId(), null,
				new NullProgressMonitor());

		// Then
		remoteBookmarksTree = gDriveRemoteBookmarksStore.load(bookmarksTree.getRootFolder().getId(),
				new NullProgressMonitor());
		assertEquals(bookmarksTree.toString(), remoteBookmarksTree.getBookmarksTree().toString());
	}

	@Test(expected = ConflictException.class)
	public void testConflictWhenSaving() throws Exception {
		// Given
		BookmarksTree bookmarksTree = new BookmarksTree(new BookmarkFolder(new BookmarkId("tree1"), Maps.newHashMap()));
		connect();
		RemoteBookmarksTree remoteBookmarksTree = gDriveRemoteBookmarksStore.add(bookmarksTree,
				bookmarksTree.getRootFolder().getId(), new NullProgressMonitor());
		BookmarksTree bookmarksTree2 = bookmarksTree.setPropertyValue(bookmarksTree.getRootFolder().getId(),
				"myProperty", "myPropertyValue1");
		gDriveRemoteBookmarksStore.save(bookmarksTree2, bookmarksTree2.getRootFolder().getId(),
				remoteBookmarksTree.getEtag(), new NullProgressMonitor());

		// When
		bookmarksTree = bookmarksTree.setPropertyValue(bookmarksTree.getRootFolder().getId(), "myProperty",
				"myPropertyValue2");
		gDriveRemoteBookmarksStore.save(bookmarksTree, bookmarksTree.getRootFolder().getId(),
				remoteBookmarksTree.getEtag(), new NullProgressMonitor());
	}

	private void disconnect() throws IOException {
		gDriveRemoteBookmarksStore.disconnect(new NullProgressMonitor());
	}

	private void connect() throws IOException {
		gDriveRemoteBookmarksStore.connect(new NullProgressMonitor());
	}

}
