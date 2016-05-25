package mesfavoris.internal.model.copy;

import static mesfavoris.testutils.BookmarksTreeTestUtil.getBookmark;
import static mesfavoris.testutils.BookmarksTreeTestUtil.getBookmarkFolder;
import static org.junit.Assert.assertEquals;

import org.chabanois.mesfavoris.internal.model.copy.BookmarksCopier;
import org.chabanois.mesfavoris.model.BookmarkFolder;
import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.model.BookmarksTree;
import org.chabanois.mesfavoris.model.modification.BookmarksTreeModifier;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import mesfavoris.testutils.BookmarksTreeBuilder;
import mesfavoris.testutils.IncrementalIDGenerator;

public class BookmarksCopierTest {
	private BookmarksTree sourceBookmarksTree;
	private BookmarksCopier bookmarksCopier;

	@Before
	public void setUp() {
		sourceBookmarksTree = new BookmarksTreeBuilder(new IncrementalIDGenerator(), 5, 3, 2).build();
		bookmarksCopier = new BookmarksCopier(sourceBookmarksTree, id -> new BookmarkId(id.toString() + "Copy"));
	}

	@Test
	public void testBookmarksCopy() {
		// Given
		BookmarkFolder rootFolder = new BookmarkFolder(new BookmarkId("root"), "root");
		BookmarksTree bookmarksTree = new BookmarksTree(rootFolder);
		BookmarksTreeModifier bookmarksTreeModifier = new BookmarksTreeModifier(bookmarksTree);

		// When
		bookmarksCopier.copy(bookmarksTreeModifier, rootFolder.getId(),
				Lists.newArrayList(getBookmarkFolder(sourceBookmarksTree, 1, 1, 1).getId()));

		// Then
		assertEquals(7, bookmarksTreeModifier.getCurrentTree().size());
	}

	@Test
	public void testBookmarksCopyWithDuplicates() {
		// Given
		BookmarkFolder rootFolder = new BookmarkFolder(new BookmarkId("root"), "root");
		BookmarksTree bookmarksTree = new BookmarksTree(rootFolder);
		BookmarksTreeModifier bookmarksTreeModifier = new BookmarksTreeModifier(bookmarksTree);

		// When
		bookmarksCopier.copy(bookmarksTreeModifier, rootFolder.getId(),
				Lists.newArrayList(getBookmark(sourceBookmarksTree, 1, 1, 1).getId(),
						getBookmark(sourceBookmarksTree, 1, 1, 1, 2).getId()));

		// Then
		assertEquals(7, bookmarksTreeModifier.getCurrentTree().size());
	}

}
