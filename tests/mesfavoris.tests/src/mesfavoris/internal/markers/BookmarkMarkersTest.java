package mesfavoris.internal.markers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IMarker;
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
import mesfavoris.model.BookmarkId;
import static org.junit.Assert.*;

public class BookmarkMarkersTest {
	public static final String PROP_LINE_NUMBER = "lineNumber";
	public static final String PROP_WORKSPACE_PATH = "workspacePath";
	private BookmarksMarkers bookmarksMarkers;
	private BookmarkDatabase bookmarkDatabase;

	@Before
	public void setUp() {
		bookmarksMarkers = BookmarksPlugin.getBookmarksMarkers();
		bookmarkDatabase = BookmarksPlugin.getBookmarkDatabase();
	}

	@Test
	public void testMarkerAddedWhenBookmarkAdded() throws Exception {
		// Given
		importProjectFromTemplate("testMarkerAddedWhenBookmarkAdded", "bookmarkMarkersTest");
		Bookmark bookmark = new Bookmark(new BookmarkId(), ImmutableMap.of(PROP_WORKSPACE_PATH,
				"/testMarkerAddedWhenBookmarkAdded/file.txt", PROP_LINE_NUMBER, "0"));

		// When
		addBookmark(bookmark);
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
		addBookmark(bookmark);
		assertNotNull(bookmarksMarkers.findMarker(bookmark.getId()));

		// When
		deleteBookmark(bookmark.getId());
		
		// Then
		assertNull(bookmarksMarkers.findMarker(bookmark.getId()));
	}

	private void addBookmark(Bookmark bookmark) throws BookmarksException {
		bookmarkDatabase.modify(bookmarksTreeModifier -> bookmarksTreeModifier.addBookmarks(
				bookmarksTreeModifier.getCurrentTree().getRootFolder().getId(), Lists.newArrayList(bookmark)));
	}

	private void deleteBookmark(BookmarkId bookmarkId) throws BookmarksException {
		bookmarkDatabase.modify(bookmarksTreeModifier -> bookmarksTreeModifier.deleteBookmark(bookmarkId, false));
	}

	private void importProjectFromTemplate(String projectName, String templateName)
			throws InvocationTargetException, InterruptedException {
		Bundle bundle = Platform.getBundle("mesfavoris.tests");
		new BundleProjectImportOperation(bundle, projectName, "/projects/" + templateName + "/").run(null);
	}

}
