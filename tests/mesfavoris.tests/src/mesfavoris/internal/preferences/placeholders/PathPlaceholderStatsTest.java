package mesfavoris.internal.preferences.placeholders;

import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmark;
import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmarkFolder;
import static mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder.bookmarksTree;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import mesfavoris.BookmarksException;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder;

public class PathPlaceholderStatsTest {
	private BookmarkDatabase bookmarkDatabase;
	private PathPlaceholderStats pathPlaceholderStats;

	@Before
	public void setUp() {
		bookmarkDatabase = new BookmarkDatabase("main", getInitialTree());
		List<String> pathPropertyNames = Lists.newArrayList("git.repositoryDir", "folderPath", "filePath");
		pathPlaceholderStats = new PathPlaceholderStats(() -> bookmarkDatabase.getBookmarksTree(), pathPropertyNames);
	}

	@Test
	public void testPlaceholderUsageCount() {
		// When
		int usageCount = pathPlaceholderStats.getUsageCount("HOME");

		// Then
		assertEquals(2, usageCount);
	}

	@Test
	public void testRefresh() throws BookmarksException {
		// Given
		BookmarkId bookmarkId = new BookmarkId("bookmark3");
		bookmarkDatabase.modify((bookmarksTreeModifier) -> bookmarksTreeModifier.setPropertyValue(bookmarkId,
				"filePath", "${HOME}/file3.txt"));

		// When
		pathPlaceholderStats.refresh();

		// Then
		assertThat(pathPlaceholderStats.getUsageCount("HOME")).isEqualTo(3);
	}

	private BookmarksTree getInitialTree() {
		BookmarksTreeBuilder bookmarksTreeBuilder = bookmarksTree("rootFolder");
		bookmarksTreeBuilder.addBookmarks("rootFolder", bookmarkFolder("bookmarkFolder1"),
				bookmarkFolder("bookmarkFolder2"));
		bookmarksTreeBuilder.addBookmarks("bookmarkFolder1",
				bookmark("bookmark1").withProperty("filePath", "${HOME}/file1.txt"),
				bookmark("bookmark2").withProperty("folderPath", "${HOME}/folder2"));
		bookmarksTreeBuilder.addBookmarks("bookmarkFolder2", bookmark("bookmark3"), bookmark("bookmark4"));

		return bookmarksTreeBuilder.build();
	}

}
