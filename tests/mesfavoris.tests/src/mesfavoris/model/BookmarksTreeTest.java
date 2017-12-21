package mesfavoris.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class BookmarksTreeTest {
	private BookmarksTree bookmarksTree;
	private BookmarkFolder rootFolder = new BookmarkFolder(new BookmarkId("rootId"), "root");
	private BookmarkFolder bookmarkFolder1 = new BookmarkFolder(new BookmarkId("folder1Id"), "folder1");
	private BookmarkFolder bookmarkFolder2 = new BookmarkFolder(new BookmarkId("folder2Id"), "folder2");
	private Bookmark bookmark1 = new Bookmark(new BookmarkId("id1"));
	private Bookmark bookmark2 = new Bookmark(new BookmarkId("id2"));
	private Bookmark bookmark3 = new Bookmark(new BookmarkId("id3"));
	private Bookmark bookmark4 = new Bookmark(new BookmarkId("id4"));
	private Bookmark bookmark5 = new Bookmark(new BookmarkId("id5"));
	private Bookmark bookmark6 = new Bookmark(new BookmarkId("id6"));

	@Before
	public void setUp() {
		bookmarksTree = new BookmarksTree(rootFolder);
		bookmarksTree = bookmarksTree.addBookmarks(rootFolder.getId(),
				Lists.newArrayList(bookmarkFolder1, bookmarkFolder2));
		bookmarksTree = bookmarksTree.addBookmarks(bookmarkFolder1.getId(),
				Lists.newArrayList(bookmark1, bookmark2, bookmark3));
		bookmarksTree = bookmarksTree.addBookmarks(bookmarkFolder2.getId(),
				Lists.newArrayList(bookmark4, bookmark5, bookmark6));
	}

	@Test
	public void testRootFolderHasNoParent() {
		// Given
		BookmarkFolder rootFolder = bookmarksTree.getRootFolder();

		// When
		BookmarkFolder parent = bookmarksTree.getParentBookmark(rootFolder.getId());

		// Then
		assertNull(parent);
	}

	@Test
	public void testGetParentBookmark() {
		// Given

		// When
		BookmarkFolder bookmarkFolder = bookmarksTree.getParentBookmark(bookmark6.getId());

		// Then
		assertEquals(bookmarkFolder2, bookmarkFolder);
	}

	@Test
	public void testGetChildren() {
		// Given

		// When
		List<Bookmark> children = bookmarksTree.getChildren(bookmarkFolder1.getId());

		// Then
		assertEquals(Lists.newArrayList(bookmark1, bookmark2, bookmark3), children);
	}

	@Test
	public void testAddBookmarks() {
		// Given
		Bookmark bookmark7 = new Bookmark(new BookmarkId("id7"));
		Bookmark bookmark8 = new Bookmark(new BookmarkId("id8"));

		// When
		bookmarksTree = bookmarksTree.addBookmarks(bookmarkFolder1.getId(), Lists.newArrayList(bookmark7, bookmark8));

		// Then
		assertEquals(Lists.newArrayList(bookmark1, bookmark2, bookmark3, bookmark7, bookmark8),
				bookmarksTree.getChildren(bookmarkFolder1.getId()));
		assertEquals(bookmarkFolder1, bookmarksTree.getParentBookmark(bookmark7.getId()));
		assertEquals(bookmarkFolder1, bookmarksTree.getParentBookmark(bookmark8.getId()));
	}

	@Test
	public void testCannotAddBookmarkTwice() {

		// When
		Throwable thrown = catchThrowable(() -> {
			bookmarksTree = bookmarksTree.addBookmarks(bookmarkFolder2.getId(), Lists.newArrayList(bookmark1));
		});

		// Then
		assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testMoveBookmarks() {
		// Given

		// When
		bookmarksTree = bookmarksTree.move(Lists.newArrayList(bookmark1.getId(), bookmark2.getId()),
				bookmarkFolder2.getId());

		// Then
		assertEquals(Lists.newArrayList(bookmark3), bookmarksTree.getChildren(bookmarkFolder1.getId()));
		assertEquals(Lists.newArrayList(bookmark4, bookmark5, bookmark6, bookmark1, bookmark2),
				bookmarksTree.getChildren(bookmarkFolder2.getId()));
	}

	@Test
	public void testMoveBookmarksToSamePlace() {
		// Given

		// When
		BookmarksTree newBookmarksTree = bookmarksTree.move(Lists.newArrayList(bookmark1.getId(), bookmark2.getId(), bookmark3.getId()),
				bookmarkFolder1.getId());		
		
		// Then
		assertSame(bookmarksTree, newBookmarksTree);
	}
	
	@Test
	public void testMoveBookmarksAfter() {
		// Given

		// When
		bookmarksTree = bookmarksTree.moveAfter(Lists.newArrayList(bookmark1.getId(), bookmark2.getId()),
				bookmarkFolder2.getId(), bookmark5.getId());

		// Then
		assertEquals(Lists.newArrayList(bookmark3), bookmarksTree.getChildren(bookmarkFolder1.getId()));
		assertEquals(Lists.newArrayList(bookmark4, bookmark5, bookmark1, bookmark2, bookmark6),
				bookmarksTree.getChildren(bookmarkFolder2.getId()));
	}

	@Test
	public void testMoveBookmarksAfterItSelf() {
		// When
		Throwable thrown = catchThrowable(() -> {
			bookmarksTree = bookmarksTree.moveAfter(Lists.newArrayList(bookmark2.getId()), bookmarkFolder1.getId(),
					bookmark2.getId());
		});

		// Then
		assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testMoveBookmarksAfterNull() {
		// Given

		// When
		bookmarksTree = bookmarksTree.moveAfter(Lists.newArrayList(bookmark1.getId(), bookmark2.getId()),
				bookmarkFolder2.getId(), null);

		// Then
		assertEquals(Lists.newArrayList(bookmark3), bookmarksTree.getChildren(bookmarkFolder1.getId()));
		assertEquals(Lists.newArrayList(bookmark1, bookmark2, bookmark4, bookmark5, bookmark6),
				bookmarksTree.getChildren(bookmarkFolder2.getId()));
	}

	@Test
	public void testMoveBookmarksAfterToSamePlace() {
		// Given

		// When
		BookmarksTree newBookmarksTree = bookmarksTree.moveAfter(Lists.newArrayList(bookmark2.getId(), bookmark3.getId()),
				bookmarkFolder1.getId(), bookmark1.getId());
		
		// Then
		assertSame(bookmarksTree, newBookmarksTree);
	}
	
	@Test
	public void testMoveBookmarksBefore() {
		// Given

		// When
		bookmarksTree = bookmarksTree.moveBefore(Lists.newArrayList(bookmark1.getId(), bookmark2.getId()),
				bookmarkFolder2.getId(), bookmark5.getId());

		// Then
		assertEquals(Lists.newArrayList(bookmark3), bookmarksTree.getChildren(bookmarkFolder1.getId()));
		assertEquals(Lists.newArrayList(bookmark4, bookmark1, bookmark2, bookmark5, bookmark6),
				bookmarksTree.getChildren(bookmarkFolder2.getId()));
	}

	@Test
	public void testMoveBookmarksBeforeToSamePlace() {
		// Given

		// When
		BookmarksTree newBookmarksTree = bookmarksTree.moveBefore(Lists.newArrayList(bookmark1.getId(), bookmark2.getId()),
				bookmarkFolder1.getId(), bookmark3.getId());

		// Then
		assertSame(bookmarksTree, newBookmarksTree);
	}	
	
	@Test
	public void testMoveBookmarksBeforeNull() {
		// Given

		// When
		bookmarksTree = bookmarksTree.moveBefore(Lists.newArrayList(bookmark1.getId(), bookmark2.getId()),
				bookmarkFolder2.getId(), null);

		// Then
		assertEquals(Lists.newArrayList(bookmark3), bookmarksTree.getChildren(bookmarkFolder1.getId()));
		assertEquals(Lists.newArrayList(bookmark4, bookmark5, bookmark6, bookmark1, bookmark2),
				bookmarksTree.getChildren(bookmarkFolder2.getId()));
	}

	@Test
	public void testSetPropertyValue() {
		// Given

		// When
		bookmarksTree = bookmarksTree.setPropertyValue(bookmark1.getId(), "prop1", "value1");
		bookmarksTree = bookmarksTree.setPropertyValue(bookmark1.getId(), "prop2", "value2");

		// Then
		assertEquals("value1", bookmarksTree.getBookmark(bookmark1.getId()).getPropertyValue("prop1"));
		assertEquals("value2", bookmarksTree.getBookmark(bookmark1.getId()).getPropertyValue("prop2"));
	}

	@Test
	public void testSetSamePropertyValue() {
		// Given
		bookmarksTree = bookmarksTree.setPropertyValue(bookmark1.getId(), "prop1", "value1");
		bookmarksTree = bookmarksTree.setPropertyValue(bookmark1.getId(), "prop2", "value2");

		// When
		BookmarksTree newBookmarkTree = bookmarksTree.setPropertyValue(bookmark1.getId(), new String("prop1"),
				new String("value1"));

		// Then
		assertSame(bookmarksTree, newBookmarkTree);
	}

	@Test
	public void testDeletePropertyValue() {
		// Given
		bookmarksTree = bookmarksTree.setPropertyValue(bookmark1.getId(), "prop1", "value1");
		bookmarksTree = bookmarksTree.setPropertyValue(bookmark1.getId(), "prop2", "value2");

		// When
		bookmarksTree = bookmarksTree.setPropertyValue(bookmark1.getId(), "prop1", null);

		// Then
		assertNull(bookmarksTree.getBookmark(bookmark1.getId()).getPropertyValue("prop1"));
		assertEquals("value2", bookmarksTree.getBookmark(bookmark1.getId()).getPropertyValue("prop2"));
	}

	@Test
	public void testSetProperties() {
		// Given
		Map<String, String> properties = Maps.newHashMap();
		properties.put("prop1", "value1");
		properties.put("prop2", "value2");

		// When
		bookmarksTree = bookmarksTree.setProperties(bookmark1.getId(), properties);

		// Then
		assertEquals("value1", bookmarksTree.getBookmark(bookmark1.getId()).getPropertyValue("prop1"));
		assertEquals("value2", bookmarksTree.getBookmark(bookmark1.getId()).getPropertyValue("prop2"));
	}

	@Test
	public void testSetSameProperties() {
		// Given
		Map<String, String> properties = Maps.newHashMap();
		properties.put("prop1", "value1");
		properties.put("prop2", "value2");
		bookmarksTree = bookmarksTree.setProperties(bookmark1.getId(), properties);

		// When
		Map<String, String> newProperties = Maps.newTreeMap();
		newProperties.put(new String("prop1"), new String("value1"));
		newProperties.put(new String("prop2"), new String("value2"));
		BookmarksTree newBookmarksTree = bookmarksTree.setProperties(bookmark1.getId(), newProperties);

		// Then
		assertSame(bookmarksTree, newBookmarksTree);
	}

	@Test
	public void testDeleteBookmark() {
		// Given

		// When
		bookmarksTree = bookmarksTree.deleteBookmark(bookmark2.getId(), false);

		// Then
		assertNull(bookmarksTree.getBookmark(bookmark2.getId()));
		assertEquals(Lists.newArrayList(bookmark1, bookmark3), bookmarksTree.getChildren(bookmarkFolder1.getId()));
	}

	@Test
	public void testDeleteBookmarkRecursively() {
		// Given
		BookmarkFolder bookmarkFolder3 = new BookmarkFolder(new BookmarkId("folder3Id"), "folder3");
		Bookmark bookmark7 = new Bookmark(new BookmarkId("id7"));
		Bookmark bookmark8 = new Bookmark(new BookmarkId("id8"));
		bookmarksTree = bookmarksTree.addBookmarks(bookmarkFolder2.getId(), Lists.newArrayList(bookmarkFolder3));
		bookmarksTree = bookmarksTree.addBookmarks(bookmarkFolder3.getId(), Lists.newArrayList(bookmark7, bookmark8));

		// When
		bookmarksTree = bookmarksTree.deleteBookmark(bookmarkFolder2.getId(), true);

		// Then
		assertNull(bookmarksTree.getBookmark(bookmarkFolder2.getId()));
		assertNull(bookmarksTree.getBookmark(bookmark4.getId()));
		assertNull(bookmarksTree.getBookmark(bookmark5.getId()));
		assertNull(bookmarksTree.getBookmark(bookmark6.getId()));
		assertNull(bookmarksTree.getBookmark(bookmarkFolder3.getId()));
		assertNull(bookmarksTree.getBookmark(bookmark7.getId()));
		assertNull(bookmarksTree.getBookmark(bookmark8.getId()));
	}

	@Test
	public void testCannotDeleteNonEmptyBookmarkFolder() {
		// When
		Throwable thrown = catchThrowable(() -> {
			bookmarksTree = bookmarksTree.deleteBookmark(bookmarkFolder2.getId(), false);
		});

		// Then
		assertThat(thrown).isInstanceOf(IllegalStateException.class);
	}

	@Test
	public void testSubTree() {
		// Given

		// When
		BookmarksTree subTree = bookmarksTree.subTree(bookmarkFolder1.getId());

		// Then
		assertEquals(bookmarkFolder1, subTree.getRootFolder());
	}

}
