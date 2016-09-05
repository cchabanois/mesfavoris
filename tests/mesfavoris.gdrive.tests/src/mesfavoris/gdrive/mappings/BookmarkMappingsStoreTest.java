package mesfavoris.gdrive.mappings;

import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmark;
import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmarkFolder;
import static mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder.bookmarksTree;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.eclipse.core.runtime.IProgressMonitor;
import org.junit.Before;
import org.junit.Test;

import com.google.api.services.drive.model.File;

import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder;
import mesfavoris.tests.commons.waits.Waiter;

public class BookmarkMappingsStoreTest {
	private BookmarkMappingsStore bookmarkMappingsStore;
	private IBookmarkMappingsPersister bookmarkMappingsPersister = mock(IBookmarkMappingsPersister.class);
	private IBookmarkMappingsListener listener = mock(IBookmarkMappingsListener.class);
	private BookmarkDatabase bookmarkDatabase;
	
	@Before
	public void setUp() {
		bookmarkMappingsStore = new BookmarkMappingsStore(bookmarkMappingsPersister);
		bookmarkMappingsStore.addListener(listener);
		bookmarkDatabase = new BookmarkDatabase("test", createBookmarksTree());
		bookmarkDatabase.addListener(bookmarkMappingsStore);
	}

	private BookmarksTree createBookmarksTree() {
		BookmarksTreeBuilder bookmarksTreeBuilder = bookmarksTree("root");
		bookmarksTreeBuilder.addBookmarks("root", bookmarkFolder("folder1"), bookmarkFolder("folder2"));
		bookmarksTreeBuilder.addBookmarks("folder1", bookmarkFolder("folder11"), bookmark("bookmark11"));
		
		return bookmarksTreeBuilder.build();
	}
	
	@Test
	public void testAddMapping() throws Exception {
		// Given
		File file = new File();
		file.setId("fileId");
		BookmarkId bookmarkFolderId = new BookmarkId("folder1");

		// When
		bookmarkMappingsStore.add(bookmarkFolderId, file);

		// Then
		verify(listener).mappingAdded(bookmarkFolderId);
		Waiter.waitUntil("persister save not called", () -> {
			verify(bookmarkMappingsPersister).save(anySet(), any(IProgressMonitor.class));
			return true;
		});
		assertEquals("fileId", bookmarkMappingsStore.getMapping(bookmarkFolderId).get().getFileId());
		assertEquals(bookmarkFolderId, bookmarkMappingsStore.getMapping("fileId").get().getBookmarkFolderId());
	}

	@Test
	public void testMappingRemovedIfFolderDeleted() throws Exception {
		// Given
		File file = new File();
		file.setId("fileId");
		BookmarkId bookmarkFolderId = new BookmarkId("folder1");
		bookmarkMappingsStore.add(bookmarkFolderId, file);
		
		// When
		bookmarkDatabase.modify(bookmarksTreeModifier->bookmarksTreeModifier.deleteBookmark(bookmarkFolderId, true));
		
		// Then
		assertFalse(bookmarkMappingsStore.getMapping("fileId").isPresent());
	}
	
	@Test
	public void testMappingRemovedIfParentFolderDeleted() throws Exception {
		// Given
		bookmarkMappingsStore.add(new BookmarkId("folder11"), file("file11Id", "folder11.xml"));
		bookmarkMappingsStore.add(new BookmarkId("folder2"), file("file2Id", "folder2.xml"));
		
		// When
		bookmarkDatabase.modify(bookmarksTreeModifier->bookmarksTreeModifier.deleteBookmark(new BookmarkId("folder1"), true));
		
		// Then
		assertFalse(bookmarkMappingsStore.getMapping("file11Id").isPresent());
		assertTrue(bookmarkMappingsStore.getMapping("file2Id").isPresent());
	}
	
	private File file(String id, String title) {
		File file = new File();
		file.setId(id);
		file.setTitle(title);
		return file;
	}
	
}
