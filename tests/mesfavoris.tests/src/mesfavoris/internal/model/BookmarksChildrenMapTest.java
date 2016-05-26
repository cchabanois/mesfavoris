package mesfavoris.internal.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import mesfavoris.internal.model.BookmarksChildrenMap;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;

public class BookmarksChildrenMapTest {
	private BookmarksChildrenMap bookmarksChildrenMap;
	private BookmarkFolder bookmarkFolder1 = new BookmarkFolder(new BookmarkId("folder1Id"), "folder1");
	private BookmarkFolder bookmarkFolder2 = new BookmarkFolder(new BookmarkId("folder2Id"), "folder2");
	private Bookmark bookmark1 = new Bookmark(new BookmarkId("id1"));
	private Bookmark bookmark2 = new Bookmark(new BookmarkId("id2"));
	private Bookmark bookmark3 = new Bookmark(new BookmarkId("id3"));

	@Before
	public void setUp() {
		bookmarksChildrenMap = new BookmarksChildrenMap();
		bookmarksChildrenMap = bookmarksChildrenMap.add(bookmarkFolder1.getId(),
				Lists.newArrayList(bookmark1.getId(), bookmark2.getId(), bookmark3.getId()));
	}

	@Test
	public void testAddToEmptyFolder() {
		// Given
		Bookmark bookmark4 = new Bookmark(new BookmarkId("id4"));
		Bookmark bookmark5 = new Bookmark(new BookmarkId("id5"));

		// When
		bookmarksChildrenMap = bookmarksChildrenMap.add(bookmarkFolder2.getId(),
				Lists.newArrayList(bookmark4.getId(), bookmark5.getId()));

		// Then
		assertTrue(bookmarksChildrenMap.hasChildren(bookmarkFolder2.getId()));
		assertEquals(Lists.newArrayList(bookmark4.getId(), bookmark5.getId()),
				bookmarksChildrenMap.getChildren(bookmarkFolder2.getId()));
	}

	@Test
	public void testAddToNonEmptyFolder() {
		// Given
		Bookmark bookmark4 = new Bookmark(new BookmarkId("id4"));
		Bookmark bookmark5 = new Bookmark(new BookmarkId("id5"));

		// When
		bookmarksChildrenMap = bookmarksChildrenMap.add(bookmarkFolder1.getId(),
				Lists.newArrayList(bookmark4.getId(), bookmark5.getId()));

		// Then
		assertEquals(Lists.newArrayList(bookmark1.getId(), bookmark2.getId(), bookmark3.getId(), bookmark4.getId(),
				bookmark5.getId()), bookmarksChildrenMap.getChildren(bookmarkFolder1.getId()));
	}

	@Test
	public void testDeleteNonEmptyFolder() {
		// Given

		// When
		bookmarksChildrenMap = bookmarksChildrenMap.delete(bookmarkFolder1.getId());

		// Then
		assertFalse(bookmarksChildrenMap.hasChildren(bookmarkFolder1.getId()));
	}

}
