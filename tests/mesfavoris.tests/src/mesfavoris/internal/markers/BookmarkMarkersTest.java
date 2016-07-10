package mesfavoris.internal.markers;

import static mesfavoris.tests.commons.waits.Waiter.waitUntil;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import mesfavoris.BookmarksException;
import mesfavoris.BookmarksPlugin;
import mesfavoris.commons.ui.wizards.datatransfer.BundleProjectImportOperation;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;

public class BookmarkMarkersTest {
	public static final String PROP_LINE_NUMBER = "lineNumber";
	public static final String PROP_WORKSPACE_PATH = "workspacePath";
	private BookmarksMarkers bookmarksMarkers;
	private BookmarkDatabase bookmarkDatabase;
	private BookmarkId rootFolderId;

	@Before
	public void setUp() {
		bookmarksMarkers = BookmarksPlugin.getBookmarksMarkers();
		bookmarkDatabase = BookmarksPlugin.getBookmarkDatabase();
		rootFolderId = bookmarkDatabase.getBookmarksTree().getRootFolder().getId();
	}

	@Test
	public void testMarkerAddedWhenBookmarkAdded() throws Exception {
		// Given
		importProjectFromTemplate("testMarkerAddedWhenBookmarkAdded", "bookmarkMarkersTest");
		Bookmark bookmark = new Bookmark(new BookmarkId(), ImmutableMap.of(PROP_WORKSPACE_PATH,
				"/testMarkerAddedWhenBookmarkAdded/file.txt", PROP_LINE_NUMBER, "0"));

		// When
		addBookmark(rootFolderId, bookmark);
		IMarker marker = bookmarksMarkers.findMarker(bookmark.getId());

		// Then
		assertNotNull(marker);
		assertEquals(1, marker.getAttribute(IMarker.LINE_NUMBER));
	}

	@Test
	public void testMarkerDeletedWhenBookmarkDeleted() throws Exception {
		// Given
		importProjectFromTemplate("testMarkerDeletedWhenBookmarkDeleted", "bookmarkMarkersTest");
		Bookmark bookmark = new Bookmark(new BookmarkId(), ImmutableMap.of(PROP_WORKSPACE_PATH,
				"/testMarkerDeletedWhenBookmarkDeleted/file.txt", PROP_LINE_NUMBER, "0"));
		addBookmark(rootFolderId, bookmark);
		assertNotNull(bookmarksMarkers.findMarker(bookmark.getId()));

		// When
		deleteBookmark(bookmark.getId());

		// Then
		assertNull(bookmarksMarkers.findMarker(bookmark.getId()));
	}

	@Test
	public void testMarkerDeletedWhenBookmarkParentDeletedRecursively() throws Exception {
		// Given
		importProjectFromTemplate("testMarkerDeletedWhenBookmarkParentDeletedRecursively", "bookmarkMarkersTest");
		BookmarkFolder bookmarkFolder = new BookmarkFolder(new BookmarkId(), "folder");
		addBookmark(rootFolderId, bookmarkFolder);
		Bookmark bookmark = new Bookmark(new BookmarkId(), ImmutableMap.of(PROP_WORKSPACE_PATH,
				"/testMarkerDeletedWhenBookmarkDeleted/file.txt", PROP_LINE_NUMBER, "0"));
		addBookmark(bookmarkFolder.getId(), bookmark);
		assertNotNull(bookmarksMarkers.findMarker(bookmark.getId()));

		// When
		deleteBookmarkRecursively(bookmarkFolder.getId());

		// Then
		assertNull(bookmarksMarkers.findMarker(bookmark.getId()));
	}

	@Test
	public void testInvalidMarkersDeletedWhenProjectOpened() throws Exception {
		// Given
		importProjectFromTemplate("testInvalidMarkersDeletedWhenProjectOpened", "bookmarkMarkersTest");
		Bookmark bookmark = new Bookmark(new BookmarkId(), ImmutableMap.of(PROP_WORKSPACE_PATH,
				"/testInvalidMarkersDeletedWhenProjectOpened/file.txt", PROP_LINE_NUMBER, "0"));
		addBookmark(rootFolderId, bookmark);
		assertNotNull(bookmarksMarkers.findMarker(bookmark.getId()));
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject("testInvalidMarkersDeletedWhenProjectOpened");
		project.close(null);
		deleteBookmark(bookmark.getId());

		// When
		project.open(null);

		// Then
		waitUntil("Bookmark marker should be deleted", () -> bookmarksMarkers.findMarker(bookmark.getId()) == null);
	}

	private void addBookmark(BookmarkId parentId, Bookmark bookmark) throws BookmarksException {
		bookmarkDatabase.modify(
				bookmarksTreeModifier -> bookmarksTreeModifier.addBookmarks(parentId, Lists.newArrayList(bookmark)));
	}
	
	private void deleteBookmark(BookmarkId bookmarkId) throws BookmarksException {
		bookmarkDatabase.modify(bookmarksTreeModifier -> bookmarksTreeModifier.deleteBookmark(bookmarkId, false));
	}

	private void deleteBookmarkRecursively(BookmarkId bookmarkId) throws BookmarksException {
		bookmarkDatabase.modify(bookmarksTreeModifier -> bookmarksTreeModifier.deleteBookmark(bookmarkId, true));
	}
	
	
	private void importProjectFromTemplate(String projectName, String templateName)
			throws InvocationTargetException, InterruptedException {
		Bundle bundle = Platform.getBundle("mesfavoris.tests");
		new BundleProjectImportOperation(bundle, projectName, "/projects/" + templateName + "/").run(null);
	}

}
