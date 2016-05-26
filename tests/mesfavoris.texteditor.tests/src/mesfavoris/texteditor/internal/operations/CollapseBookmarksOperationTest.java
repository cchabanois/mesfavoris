package mesfavoris.texteditor.internal.operations;

import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_FILE_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import mesfavoris.BookmarksException;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.texteditor.internal.operations.CollapseBookmarksOperation;
import mesfavoris.texteditor.placeholders.PathPlaceholder;
import mesfavoris.texteditor.placeholders.PathPlaceholdersMap;
import mesfavoris.validation.IBookmarkModificationValidator;

public class CollapseBookmarksOperationTest {
	private final PathPlaceholdersMap pathPlaceholders = new PathPlaceholdersMap();
	private CollapseBookmarksOperation collapseBookmarksOperation;
	private IBookmarkModificationValidator bookmarkModificationValidator = mock(IBookmarkModificationValidator.class);

	@Before
	public void setUp() {
		pathPlaceholders.add(new PathPlaceholder("HOME", new Path("/home/cchabanois")));
		pathPlaceholders.add(new PathPlaceholder("BLT", new Path("/home/cchabanois/blt")));
		when(bookmarkModificationValidator.validateModification(any(BookmarksTree.class), any(BookmarkId.class)))
				.thenReturn(Status.OK_STATUS);
	}

	private BookmarkDatabase getBookmarkDatabase(Bookmark... bookmarks) {
		BookmarkFolder rootFolder = new BookmarkFolder(new BookmarkId("rootId"), "root");
		BookmarksTree bookmarksTree = new BookmarksTree(rootFolder);
		bookmarksTree = bookmarksTree.addBookmarks(rootFolder.getId(), Lists.newArrayList(bookmarks));
		BookmarkDatabase bookmarkDatabase = new BookmarkDatabase("id", bookmarksTree);
		return bookmarkDatabase;
	}

	@Test
	public void testCollapse() throws BookmarksException {
		// Given
		Bookmark bookmark = bookmark("bookmark", "/home/cchabanois/blt/myfile.txt");
		BookmarkDatabase bookmarkDatabase = getBookmarkDatabase(bookmark);
		collapseBookmarksOperation = new CollapseBookmarksOperation(bookmarkDatabase, pathPlaceholders,
				bookmarkModificationValidator);

		// When
		collapseBookmarksOperation.collapse(Lists.newArrayList(bookmark.getId()));

		// Then
		assertFilePath(bookmarkDatabase.getBookmarksTree(), bookmark.getId(), "${BLT}/myfile.txt");
	}

	@Test
	public void testAlreadyCollapsed() throws BookmarksException {
		// Given
		Bookmark bookmark = bookmark("bookmark", "${HOME}/blt/myfile.txt");
		BookmarkDatabase bookmarkDatabase = getBookmarkDatabase(bookmark);
		collapseBookmarksOperation = new CollapseBookmarksOperation(bookmarkDatabase, pathPlaceholders,
				bookmarkModificationValidator);

		// When
		collapseBookmarksOperation.collapse(Lists.newArrayList(bookmark.getId()));

		// Then
		assertFilePath(bookmarkDatabase.getBookmarksTree(), bookmark.getId(), "${BLT}/myfile.txt");
	}

	private Bookmark bookmark(String name, String filePath) {
		Map<String, String> properties = Maps.newHashMap();
		properties.put(Bookmark.PROPERTY_NAME, name);
		properties.put(PROP_FILE_PATH, filePath);
		return new Bookmark(new BookmarkId(), properties);
	}

	private void assertFilePath(BookmarksTree bookmarksTree, BookmarkId bookmarkId, String expectedFilePath) {
		Bookmark bookmark = bookmarksTree.getBookmark(bookmarkId);
		assertThat(bookmark).isNotNull();
		assertThat(bookmark.getPropertyValue(PROP_FILE_PATH)).isEqualTo(expectedFilePath);
	}

}
