package mesfavoris.internal.markers;

import static mesfavoris.tests.commons.waits.Waiter.waitUntil;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import mesfavoris.BookmarksException;
import mesfavoris.MesFavoris;
import mesfavoris.commons.ui.wizards.datatransfer.BundleProjectImportOperation;
import mesfavoris.markers.IBookmarksMarkers;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;

public class BookmarksMarkersTest {
	public static final String PROP_LINE_NUMBER = "lineNumber";
	public static final String PROP_WORKSPACE_PATH = "workspacePath";
	private IBookmarksMarkers bookmarksMarkers;
	private BookmarkDatabase bookmarkDatabase;
	private BookmarkId rootFolderId;

	@Before
	public void setUp() {
		bookmarksMarkers = MesFavoris.getBookmarksMarkers();
		bookmarkDatabase = MesFavoris.getBookmarkDatabase();
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
		IMarker marker = waitUntil("Cannot find marker", () -> findBookmarkMarker(bookmark.getId()));

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
		waitUntil("Cannot find marker", () -> findBookmarkMarker(bookmark.getId()));

		// When
		deleteBookmark(bookmark.getId());

		// Then
		waitUntil("Marker not deleted", () -> findBookmarkMarker(bookmark.getId()) == null);
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
		waitUntil("Cannot find marker", () -> findBookmarkMarker(bookmark.getId()));

		// When
		deleteBookmarkRecursively(bookmarkFolder.getId());

		// Then
		waitUntil("Marker not deleted", () -> findBookmarkMarker(bookmark.getId()) == null);
	}

	@Test
	public void testMarkerModifiedWhenBookmarkChanged() throws Exception {
		// Given
		importProjectFromTemplate("testMarkerModifiedWhenBookmarkChanged", "bookmarkMarkersTest");
		Bookmark bookmark = new Bookmark(new BookmarkId(), ImmutableMap.of(PROP_WORKSPACE_PATH,
				"/testMarkerModifiedWhenBookmarkChanged/file.txt", PROP_LINE_NUMBER, "0"));
		addBookmark(rootFolderId, bookmark);
		waitUntil("Cannot find marker", () -> findBookmarkMarker(bookmark.getId()));

		// When
		modifyBookmark(bookmark.getId(), PROP_LINE_NUMBER, "1");

		// Then
		waitUntil("Marker not modified",
				() -> findBookmarkMarker(bookmark.getId()).getAttribute(IMarker.LINE_NUMBER).equals(2));
	}

	@Test
	public void testMarkerReplacedWhenBookmarkResourceChanged() throws Exception {
		// Given
		importProjectFromTemplate("testMarkerReplacedWhenBookmarkResourceChanged", "bookmarkMarkersTest");
		Bookmark bookmark = new Bookmark(new BookmarkId(), ImmutableMap.of(PROP_WORKSPACE_PATH,
				"/testMarkerReplacedWhenBookmarkResourceChanged/file.txt", PROP_LINE_NUMBER, "0"));
		addBookmark(rootFolderId, bookmark);
		waitUntil("Cannot find marker", () -> findBookmarkMarker(bookmark.getId()));

		// When
		modifyBookmark(bookmark.getId(), PROP_WORKSPACE_PATH,
				"/testMarkerReplacedWhenBookmarkResourceChanged/file2.txt");

		// Then
		waitUntil("Marker not modified", () -> findBookmarkMarker(bookmark.getId()).getResource().getFullPath()
				.toString().equals("/testMarkerReplacedWhenBookmarkResourceChanged/file2.txt"));
	}

	@Test
	public void testInvalidMarkersDeletedWhenProjectOpened() throws Exception {
		// Given
		importProjectFromTemplate("testInvalidMarkersDeletedWhenProjectOpened", "bookmarkMarkersTest");
		Bookmark bookmark = new Bookmark(new BookmarkId(), ImmutableMap.of(PROP_WORKSPACE_PATH,
				"/testInvalidMarkersDeletedWhenProjectOpened/file.txt", PROP_LINE_NUMBER, "0"));
		addBookmark(rootFolderId, bookmark);
		waitUntil("Cannot find marker", () -> bookmarksMarkers.findMarker(bookmark.getId(), new NullProgressMonitor()));
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject("testInvalidMarkersDeletedWhenProjectOpened");
		project.close(null);
		deleteBookmark(bookmark.getId());

		// When
		project.open(null);

		// Then
		waitUntil("Bookmark marker should be deleted", () -> findBookmarkMarker(bookmark.getId()) == null);
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

	private void modifyBookmark(BookmarkId bookmarkId, String propertyName, String propertyValue)
			throws BookmarksException {
		bookmarkDatabase.modify(bookmarksTreeModifier -> bookmarksTreeModifier.setPropertyValue(bookmarkId,
				propertyName, propertyValue));
	}

	private void importProjectFromTemplate(String projectName, String templateName)
			throws InvocationTargetException, InterruptedException {
		Bundle bundle = Platform.getBundle("mesfavoris.tests");
		new BundleProjectImportOperation(bundle, projectName, "/projects/" + templateName + "/").run(null);
	}

	private IMarker findBookmarkMarker(BookmarkId bookmarkId) {
		return bookmarksMarkers.findMarker(bookmarkId, new NullProgressMonitor());
	}

}
