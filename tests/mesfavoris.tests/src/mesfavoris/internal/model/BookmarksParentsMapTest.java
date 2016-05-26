package mesfavoris.internal.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import mesfavoris.internal.model.BookmarksParentsMap;
import mesfavoris.model.BookmarkId;

public class BookmarksParentsMapTest {
	private BookmarksParentsMap bookmarksParentsMap;

	@Before
	public void setUp() {
		bookmarksParentsMap = new BookmarksParentsMap();
	}

	@Test
	public void testSetParent() {
		// Given

		// When
		bookmarksParentsMap = bookmarksParentsMap.setParent(new BookmarkId("bookmarkId1"), new BookmarkId("folder1Id"));

		// Then
		assertEquals(new BookmarkId("folder1Id"), bookmarksParentsMap.getParent(new BookmarkId("bookmarkId1")));
	}

	@Test
	public void delete() {
		// Given
		bookmarksParentsMap = bookmarksParentsMap.setParent(new BookmarkId("bookmarkId1"), new BookmarkId("folder1Id"));

		// When
		bookmarksParentsMap = bookmarksParentsMap.delete(new BookmarkId("bookmarkId1"));

		// Then
		assertNull(bookmarksParentsMap.getParent(new BookmarkId("bookmarkId1")));

	}
}
