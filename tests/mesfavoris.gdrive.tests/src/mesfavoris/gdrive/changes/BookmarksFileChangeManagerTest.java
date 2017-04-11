package mesfavoris.gdrive.changes;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.api.client.util.Charsets;
import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;

import mesfavoris.gdrive.GDriveTestUser;
import mesfavoris.gdrive.mappings.BookmarkMappingsStore;
import mesfavoris.gdrive.mappings.IBookmarkMappingsPersister;
import mesfavoris.gdrive.operations.CreateFileOperation;
import mesfavoris.gdrive.operations.UpdateFileOperation;
import mesfavoris.gdrive.test.GDriveConnectionRule;
import mesfavoris.model.BookmarkId;
import mesfavoris.tests.commons.waits.Waiter;

public class BookmarksFileChangeManagerTest {

	@Rule
	public GDriveConnectionRule gdriveConnectionRule = new GDriveConnectionRule(GDriveTestUser.USER1, true);

	private BookmarksFileChangeManager bookmarksFileChangeManager;
	private BookmarkMappingsStore bookmarkMappings;
	private BookmarksFileChangeListener listener = new BookmarksFileChangeListener();

	@Before
	public void setUp() throws Exception {
		bookmarkMappings = new BookmarkMappingsStore(mock(IBookmarkMappingsPersister.class));
		bookmarkMappings.add(new BookmarkId("bookmarkFolder1"),
				createFile("bookmarks1", "bookmarks for folder1").getId(), Collections.emptyMap());
		bookmarkMappings.add(new BookmarkId("bookmarkFolder2"),
				createFile("bookmarks1", "bookmarks for folder2").getId(), Collections.emptyMap());
		bookmarksFileChangeManager = new BookmarksFileChangeManager(gdriveConnectionRule.getGDriveConnectionManager(),
				bookmarkMappings, () -> Duration.ofMillis(100));
		bookmarksFileChangeManager.addListener(listener);
		bookmarksFileChangeManager.init();
	}

	@After
	public void tearDown() {
		bookmarksFileChangeManager.removeListener(listener);
		bookmarksFileChangeManager.close();
	}

	@Test
	public void testListenerCalledWhenChangeInBookmarksFile() throws Exception {
		// When
		updateFile(bookmarkMappings.getMapping(new BookmarkId("bookmarkFolder1")).get().getFileId(),
				"new bookmarks for folder1");

		// Then
		Waiter.waitUntil("Listener not called", () -> listener.getEvents().size() == 1);
		Thread.sleep(100);
		assertEquals(1, listener.getEvents().size());
		assertEquals(new BookmarkId("bookmarkFolder1"), listener.getEvents().get(0).bookmarkFolderId);
	}

	@Test
	public void testListenerNotCalledIfClosed() throws Exception {
		// Given
		bookmarksFileChangeManager.close();

		// When
		updateFile(bookmarkMappings.getMapping(new BookmarkId("bookmarkFolder1")).get().getFileId(),
				"new bookmarks for folder1");

		// Then
		Thread.sleep(300);
		assertEquals(0, listener.getEvents().size());
	}

	@Test
	public void testListenerNotCalledIfChangeInAFileThatIsNotABookmarkFile() throws Exception {
		// Given
		File file = createFile("notABookmarkFile", "not bookmarks");

		// When
		updateFile(file.getId(), "really not bookmarks");
		Thread.sleep(300);

		// Then
		assertEquals(0, listener.getEvents().size());
	}

	private File createFile(String name, String contents) throws IOException {
		CreateFileOperation createFileOperation = new CreateFileOperation(gdriveConnectionRule.getDrive());
		byte[] bytes = contents.getBytes("UTF-8");
		File file = createFileOperation.createFile(gdriveConnectionRule.getApplicationFolderId(), name, bytes,
				new NullProgressMonitor());
		return file;
	}

	private void updateFile(String fileId, String newContents) throws IOException {
		UpdateFileOperation updateFileOperation = new UpdateFileOperation(gdriveConnectionRule.getDrive());
		updateFileOperation.updateFile(fileId, newContents.getBytes(Charsets.UTF_8), null, new NullProgressMonitor());
	}

	private static class BookmarksFileChangeListener implements IBookmarksFileChangeListener {
		private final List<BookmarksFileChange> events = new ArrayList<>();

		@Override
		public void bookmarksFileChanged(BookmarkId bookmarkFolderId, Change change) {
			events.add(new BookmarksFileChange(bookmarkFolderId, change));
		}

		public List<BookmarksFileChange> getEvents() {
			return events;
		}

	}

	private static class BookmarksFileChange {
		private final BookmarkId bookmarkFolderId;
		private final Change change;

		public BookmarksFileChange(BookmarkId bookmarkFolderId, Change change) {
			this.bookmarkFolderId = bookmarkFolderId;
			this.change = change;
		}
	}

}
