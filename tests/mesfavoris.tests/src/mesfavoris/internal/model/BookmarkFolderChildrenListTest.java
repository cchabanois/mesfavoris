package mesfavoris.internal.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import mesfavoris.internal.model.BookmarkFolderChildrenList;
import mesfavoris.model.BookmarkId;

public class BookmarkFolderChildrenListTest {
	private BookmarkFolderChildrenList bookmarkFolderChildrenList;
	private BookmarkId bookmark1 = new BookmarkId("id1");
	private BookmarkId bookmark2 = new BookmarkId("id2");
	private BookmarkId bookmark3 = new BookmarkId("id3");

	@Before
	public void setUp() {
		bookmarkFolderChildrenList = new BookmarkFolderChildrenList(
				Lists.newArrayList(bookmark1, bookmark2, bookmark3));
	}

	@Test
	public void testAdd() {
		// Given
		BookmarkId bookmark4 = new BookmarkId("id4");
		BookmarkId bookmark5 = new BookmarkId("id5");

		// When
		bookmarkFolderChildrenList = bookmarkFolderChildrenList.add(Lists.newArrayList(bookmark4, bookmark5));

		// Then
		assertEquals(Lists.newArrayList(bookmark1, bookmark2, bookmark3, bookmark4, bookmark5),
				bookmarkFolderChildrenList.getBookmarks());
	}

	@Test
	public void testAddNothing() {
		// Given
		BookmarkFolderChildrenList initialList = bookmarkFolderChildrenList;

		// When
		bookmarkFolderChildrenList = bookmarkFolderChildrenList.add(new ArrayList<BookmarkId>());

		// Then
		assertSame(initialList, bookmarkFolderChildrenList);
	}

	@Test
	public void testAddBefore() {
		// Given
		BookmarkId bookmark4 = new BookmarkId("id4");
		BookmarkId bookmark5 = new BookmarkId("id5");

		// When
		bookmarkFolderChildrenList = bookmarkFolderChildrenList.addBefore(Lists.newArrayList(bookmark4, bookmark5),
				bookmark2);

		// Then
		assertEquals(Lists.newArrayList(bookmark1, bookmark4, bookmark5, bookmark2, bookmark3),
				bookmarkFolderChildrenList.getBookmarks());
	}

	@Test
	public void testAddAfter() {
		// Given
		BookmarkId bookmark4 = new BookmarkId("id4");
		BookmarkId bookmark5 = new BookmarkId("id5");

		// When
		bookmarkFolderChildrenList = bookmarkFolderChildrenList.addAfter(Lists.newArrayList(bookmark4, bookmark5),
				bookmark1);

		// Then
		assertEquals(Lists.newArrayList(bookmark1, bookmark4, bookmark5, bookmark2, bookmark3),
				bookmarkFolderChildrenList.getBookmarks());
	}

	@Test
	public void testRemove() {
		// Given

		// When
		bookmarkFolderChildrenList = bookmarkFolderChildrenList.remove(Lists.newArrayList(bookmark2));

		// Then
		assertEquals(Lists.newArrayList(bookmark1, bookmark3), bookmarkFolderChildrenList.getBookmarks());
	}

}
