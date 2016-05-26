package mesfavoris.internal.model.merge;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import mesfavoris.internal.model.merge.BookmarksTreeIterable;
import mesfavoris.internal.model.merge.BookmarksTreeIterable.Algorithm;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;

public class BookmarksTreeIterableTest {
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
	public void testPreOrder() {
		// Given
		BookmarksTreeIterable bookmarksTreeIterable = new BookmarksTreeIterable(bookmarksTree,
				bookmarksTree.getRootFolder().getId(), Algorithm.PRE_ORDER);

		// When
		List<Bookmark> orderedBookmarks = Lists.newArrayList(bookmarksTreeIterable);

		// Then
		assertEquals(Lists.newArrayList(rootFolder, bookmarkFolder1, bookmark1, bookmark2, bookmark3, bookmarkFolder2,
				bookmark4, bookmark5, bookmark6), orderedBookmarks);
	}

	@Test
	public void testPostOrder() {
		// Given
		BookmarksTreeIterable bookmarksTreeIterable = new BookmarksTreeIterable(bookmarksTree,
				bookmarksTree.getRootFolder().getId(), Algorithm.POST_ORDER);

		// When
		List<Bookmark> orderedBookmarks = Lists.newArrayList(bookmarksTreeIterable);

		// Then
		assertEquals(Lists.newArrayList(bookmark1, bookmark2, bookmark3, bookmarkFolder1, bookmark4, bookmark5,
				bookmark6, bookmarkFolder2, rootFolder), orderedBookmarks);
	}

}
