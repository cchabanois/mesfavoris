package mesfavoris.internal.model.copy;

import static mesfavoris.tests.commons.bookmarks.BookmarksTreeTestUtil.getBookmark;
import static mesfavoris.tests.commons.bookmarks.BookmarksTreeTestUtil.getBookmarkFolder;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import mesfavoris.internal.model.copy.BookmarksCopier;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.model.modification.BookmarksTreeModifier;
import mesfavoris.tests.commons.bookmarks.BookmarksTreeGenerator;
import mesfavoris.tests.commons.bookmarks.IncrementalIDGenerator;

public class BookmarksCopierTest {
	private BookmarksTree sourceBookmarksTree;
	private BookmarksCopier bookmarksCopier;

	@Before
	public void setUp() {
		sourceBookmarksTree = new BookmarksTreeGenerator(new IncrementalIDGenerator(), 5, 3, 2).build();
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
