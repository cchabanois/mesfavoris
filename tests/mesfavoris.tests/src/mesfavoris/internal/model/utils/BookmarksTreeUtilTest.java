package mesfavoris.internal.model.utils;

import static mesfavoris.testutils.BookmarksTreeTestUtil.getBookmark;
import static mesfavoris.testutils.BookmarksTreeTestUtil.getBookmarkFolder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import mesfavoris.internal.model.utils.BookmarksTreeUtils;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.testutils.BookmarksTreeGenerator;
import mesfavoris.testutils.IncrementalIDGenerator;

public class BookmarksTreeUtilTest {
	private BookmarksTree bookmarksTree;

	@Before
	public void setUp() {
		bookmarksTree = new BookmarksTreeGenerator(new IncrementalIDGenerator(), 5, 3, 2).build();
	}

	@Test
	public void testAnyBookmarkIsUnderRoot() {
		// Given
		BookmarkFolder rootFolder = bookmarksTree.getRootFolder();
		Bookmark anyBookmark = getBookmark(bookmarksTree, 1, 1, 1);

		// When
		boolean isUnder = BookmarksTreeUtils.isBookmarkUnder(bookmarksTree, anyBookmark.getId(), rootFolder.getId());

		// Then
		assertTrue(isUnder);
	}

	@Test
	public void testBookmarkIsNotUnderItself() {
		// Given
		BookmarkFolder anyBookmarkFolder = getBookmarkFolder(bookmarksTree, 1, 1, 1);

		// When
		boolean isUnder = BookmarksTreeUtils.isBookmarkUnder(bookmarksTree, anyBookmarkFolder.getId(), anyBookmarkFolder.getId());

		// Then
		assertFalse(isUnder);
	}

	@Test
	public void testUnduplicatedBookmarks() {
		// Given
		BookmarkFolder bookmarkFolder = getBookmarkFolder(bookmarksTree, 1, 1, 1);
		Bookmark bookmark = getBookmark(bookmarksTree, 1, 1, 1, 2);
		
		// When
		List<BookmarkId> unduplicatedBookmarks = BookmarksTreeUtils.getUnduplicatedBookmarks(bookmarksTree, Lists.newArrayList(bookmarkFolder.getId(), bookmark.getId()));
		
		// Then
		assertEquals(Lists.newArrayList(bookmarkFolder.getId()), unduplicatedBookmarks);
	}
	
}
