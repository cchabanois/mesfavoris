package mesfavoris.internal.model.merge;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import mesfavoris.internal.model.merge.BookmarksTreeIterator.Algorithm;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;

public class BookmarksTreeIterableTest {
	private BookmarksTree bookmarksTree;
	private BookmarkFolder f = new BookmarkFolder(new BookmarkId("f"), "f");
	private BookmarkFolder b = new BookmarkFolder(new BookmarkId("b"), "b");
	private BookmarkFolder g = new BookmarkFolder(new BookmarkId("g"), "g");
	private BookmarkFolder d = new BookmarkFolder(new BookmarkId("d"), "d");
	private BookmarkFolder i = new BookmarkFolder(new BookmarkId("i"), "i");
	private Bookmark a = new Bookmark(new BookmarkId("a"));
	private Bookmark c = new Bookmark(new BookmarkId("c"));
	private Bookmark e = new Bookmark(new BookmarkId("e"));
	private Bookmark h = new Bookmark(new BookmarkId("h"));

	@Before
	public void setUp() {
		bookmarksTree = new BookmarksTree(f);
		bookmarksTree = bookmarksTree.addBookmarks(f.getId(),
				Lists.newArrayList(b, g));
		bookmarksTree = bookmarksTree.addBookmarks(b.getId(),
				Lists.newArrayList(a, d));
		bookmarksTree = bookmarksTree.addBookmarks(d.getId(),
				Lists.newArrayList(c, e));
		bookmarksTree = bookmarksTree.addBookmarks(g.getId(),
				Lists.newArrayList(i));
		bookmarksTree = bookmarksTree.addBookmarks(i.getId(),
				Lists.newArrayList(h));
	}

	/**
	 * <img src="doc-files/Sorted_binary_tree_preorder.png" />
	 * Pre-order: F, B, A, D, C, E, G, I, H.
	 */
	@Test
	public void testPreOrder() {
		// Given
		BookmarksTreeIterable bookmarksTreeIterable = new BookmarksTreeIterable(bookmarksTree,
				bookmarksTree.getRootFolder().getId(), Algorithm.PRE_ORDER);

		// When
		List<Bookmark> orderedBookmarks = Lists.newArrayList(bookmarksTreeIterable);

		// Then
		assertEquals(Lists.newArrayList(f, b, a, d, c, e,
				g, i, h), orderedBookmarks);
	}

	/**
	 * <img src="doc-files/Sorted_binary_tree_postorder.png" />
	 * Post-order: A, C, E, D, B, H, I, G, F.
	 */
	@Test
	public void testPostOrder() {
		// Given
		BookmarksTreeIterable bookmarksTreeIterable = new BookmarksTreeIterable(bookmarksTree,
				bookmarksTree.getRootFolder().getId(), Algorithm.POST_ORDER);

		// When
		List<Bookmark> orderedBookmarks = Lists.newArrayList(bookmarksTreeIterable);

		// Then
		assertEquals(Lists.newArrayList(a, c, e, d, b, h,
				i, g, f), orderedBookmarks);
	}

}
