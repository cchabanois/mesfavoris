package mesfavoris.internal.model;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import mesfavoris.internal.model.BookmarksMap;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;

public class BookmarksMapTest {
	private BookmarksMap bookmarksMap;
	private Bookmark bookmark1 = new Bookmark(new BookmarkId("id1"));
	private Bookmark bookmark2 = new Bookmark(new BookmarkId("id2"));
	private Bookmark bookmark3 = new Bookmark(new BookmarkId("id3"));	
	
	@Before
	public void setUp() {
		bookmarksMap = new BookmarksMap();
		bookmarksMap.add(Lists.newArrayList(bookmark1, bookmark2, bookmark3));
	}
	
	@Test
	public void testDelete() {
		// Given
		
		// When
		bookmarksMap = bookmarksMap.delete(bookmark2.getId());
		
		// Then
		assertNull(bookmarksMap.get(bookmark2.getId()));
	}
	
	@Test
	public void testAdd() {
		// Given
		Bookmark bookmark4 = new Bookmark(new BookmarkId("id4"));	
		
		// When
		bookmarksMap = bookmarksMap.add(bookmark4);
		
		// Then
		assertNotNull(bookmarksMap.get(bookmark4.getId()));
	}
	
	
}
