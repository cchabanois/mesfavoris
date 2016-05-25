package mesfavoris.internal.model.merge;

import static mesfavoris.testutils.BookmarksTreeTestUtil.getBookmark;
import static mesfavoris.testutils.BookmarksTreeTestUtil.getBookmarkFolder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.chabanois.mesfavoris.internal.model.merge.BookmarksTreeMerger;
import org.chabanois.mesfavoris.model.Bookmark;
import org.chabanois.mesfavoris.model.BookmarkFolder;
import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.model.BookmarksTree;
import org.chabanois.mesfavoris.model.modification.BookmarkDeletedModification;
import org.chabanois.mesfavoris.model.modification.BookmarkPropertiesModification;
import org.chabanois.mesfavoris.model.modification.BookmarksAddedModification;
import org.chabanois.mesfavoris.model.modification.BookmarksModification;
import org.chabanois.mesfavoris.model.modification.BookmarksMovedModification;
import org.chabanois.mesfavoris.model.modification.BookmarksTreeModifier;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import mesfavoris.testutils.BookmarksTreeBuilder;
import mesfavoris.testutils.IDGenerator;
import mesfavoris.testutils.IncrementalIDGenerator;

public class BookmarksTreeMergerTest {
	private BookmarksTree originalBookmarksTree;
	private BookmarksTreeModifier bookmarksTreeModifier;
	private final IDGenerator idGenerator = new IncrementalIDGenerator();

	@Before
	public void setUp() {
		originalBookmarksTree = new BookmarksTreeBuilder(idGenerator, 5, 3, 2).build();
		bookmarksTreeModifier = new BookmarksTreeModifier(originalBookmarksTree);
	}

	@Test
	public void testBookmarkMovedToSame() {
		// Moving bookmark 64 to same folder 45 after 46
		// Moving bookmark 46 to same folder 45 after 65
		BookmarksTree newBookmarksTree = originalBookmarksTree.moveAfter(Lists.newArrayList(new BookmarkId("64")),
				new BookmarkId("45"), new BookmarkId("46"));
		newBookmarksTree = originalBookmarksTree.moveAfter(Lists.newArrayList(new BookmarkId("46")),
				new BookmarkId("45"), new BookmarkId("65"));

		merge(newBookmarksTree);
		assertEquals(newBookmarksTree.toString(), bookmarksTreeModifier.getCurrentTree().toString());
	}

	@Test
	public void testBookmarkAdded() {
		// Given
		BookmarkFolder parentFolder = getBookmarkFolder(originalBookmarksTree, 0, 0);
		Bookmark bookmarkBefore = getBookmark(originalBookmarksTree, 0, 0, 2);
		Bookmark newBookmark = new Bookmark(new BookmarkId("newBookmarkId"));
		BookmarksTree newBookmarksTree = originalBookmarksTree.addBookmarksAfter(parentFolder.getId(),
				bookmarkBefore.getId(), Arrays.asList(newBookmark));

		// When
		merge(newBookmarksTree);

		// Then
		assertEquals(newBookmarksTree.toString(), bookmarksTreeModifier.getCurrentTree().toString());
		List<BookmarksModification> modifications = bookmarksTreeModifier.getModifications();
		assertEquals(1, modifications.size());
		BookmarksAddedModification modification = assertBookmarksModificationIs(modifications.get(0),
				BookmarksAddedModification.class);
		assertEquals(parentFolder.getId(), modification.getParentId());
	}

	@Test
	public void testBookmarkDeleted() {
		// Given
		Bookmark deletedBookmark = getBookmark(originalBookmarksTree, 0, 0, 3);
		BookmarksTree newBookmarksTree = originalBookmarksTree.deleteBookmark(deletedBookmark.getId(), false);

		// When
		merge(newBookmarksTree);

		// Then
		assertEquals(newBookmarksTree.toString(), bookmarksTreeModifier.getCurrentTree().toString());
		List<BookmarksModification> modifications = bookmarksTreeModifier.getModifications();
		assertEquals(1, modifications.size());
		BookmarkDeletedModification modification = assertBookmarksModificationIs(modifications.get(0),
				BookmarkDeletedModification.class);
		assertEquals(deletedBookmark.getId(), modification.getBookmarkId());
	}

	@Test
	public void testBookmarkFolderDeletedRecursively() {
		// Given
		Bookmark deletedBookmark = getBookmark(originalBookmarksTree, 0, 1);
		BookmarksTree newBookmarksTree = originalBookmarksTree.deleteBookmark(deletedBookmark.getId(), true);

		// When
		merge(newBookmarksTree);

		// Then
		assertEquals(newBookmarksTree.toString(), bookmarksTreeModifier.getCurrentTree().toString());
		List<BookmarksModification> modifications = bookmarksTreeModifier.getModifications();
		assertEquals(1, modifications.size());
		BookmarkDeletedModification modification = assertBookmarksModificationIs(modifications.get(0),
				BookmarkDeletedModification.class);
		assertEquals(deletedBookmark.getId(), modification.getBookmarkId());
		assertTrue(modification.isRecursive());
	}

	@Test
	public void testBookmarkMovedToAnotherFolder() {
		// Given
		BookmarkFolder newParentFolder = getBookmarkFolder(originalBookmarksTree, 0, 0);
		Bookmark bookmarkBefore = getBookmark(originalBookmarksTree, 0, 0, 2);
		Bookmark movedBookmark = getBookmark(originalBookmarksTree, 0, 1, 1);
		BookmarksTree newBookmarksTree = originalBookmarksTree.moveAfter(Lists.newArrayList(movedBookmark.getId()),
				newParentFolder.getId(), bookmarkBefore.getId());

		// When
		merge(newBookmarksTree);

		// Then
		assertEquals(newBookmarksTree.toString(), bookmarksTreeModifier.getCurrentTree().toString());
		List<BookmarksModification> modifications = bookmarksTreeModifier.getModifications();
		assertEquals(1, modifications.size());
		BookmarksMovedModification modification = assertBookmarksModificationIs(modifications.get(0),
				BookmarksMovedModification.class);
		assertEquals(newParentFolder.getId(), modification.getNewParentId());
	}

	@Test
	public void testBookmarkMovedToAnotherFolderAtFirstPosition() {
		// Given
		BookmarkFolder newParentFolder = getBookmarkFolder(originalBookmarksTree, 0, 0);
		Bookmark movedBookmark = getBookmark(originalBookmarksTree, 0, 1, 1);
		BookmarksTree newBookmarksTree = originalBookmarksTree.moveAfter(Lists.newArrayList(movedBookmark.getId()),
				newParentFolder.getId(), null);

		// When
		merge(newBookmarksTree);

		// Then
		assertEquals(newBookmarksTree.toString(), bookmarksTreeModifier.getCurrentTree().toString());
		List<BookmarksModification> modifications = bookmarksTreeModifier.getModifications();
		assertEquals(1, modifications.size());
		BookmarksMovedModification modification = assertBookmarksModificationIs(modifications.get(0),
				BookmarksMovedModification.class);
		assertEquals(newParentFolder.getId(), modification.getNewParentId());
	}

	@Test
	public void testBookmarkMovedInTheSameFolder() {
		// Given
		BookmarkFolder parentFolder = getBookmarkFolder(originalBookmarksTree, 0, 0);
		Bookmark bookmarkBefore = getBookmark(originalBookmarksTree, 0, 0, 1);
		Bookmark movedBookmark = getBookmark(originalBookmarksTree, 0, 0, 4);
		BookmarksTree newBookmarksTree = originalBookmarksTree.moveAfter(Lists.newArrayList(movedBookmark.getId()),
				parentFolder.getId(), bookmarkBefore.getId());

		// When
		merge(newBookmarksTree);

		// Then
		assertEquals(newBookmarksTree.toString(), bookmarksTreeModifier.getCurrentTree().toString());
		List<BookmarksModification> modifications = bookmarksTreeModifier.getModifications();
		assertEquals(1, modifications.size());
		BookmarksMovedModification modification = assertBookmarksModificationIs(modifications.get(0),
				BookmarksMovedModification.class);
		assertEquals(parentFolder.getId(), modification.getNewParentId());
	}

	@Test
	public void testBookmarkModified() {
		// Given
		Bookmark bookmark = getBookmark(originalBookmarksTree, 0, 0, 2);
		BookmarksTree newBookmarksTree = originalBookmarksTree.setPropertyValue(bookmark.getId(), "myProperty",
				"myValue");

		// When
		merge(newBookmarksTree);

		// Then
		assertEquals(newBookmarksTree.toString(), bookmarksTreeModifier.getCurrentTree().toString());
		List<BookmarksModification> modifications = bookmarksTreeModifier.getModifications();
		assertEquals(1, modifications.size());
		BookmarkPropertiesModification modification = assertBookmarksModificationIs(modifications.get(0),
				BookmarkPropertiesModification.class);
		assertEquals(bookmark.getId(), modification.getBookmarkId());
	}

	private <T> T assertBookmarksModificationIs(BookmarksModification modification, Class<T> type) {
		assertTrue(type.isInstance(modification));
		return type.cast(modification);
	}

	private void merge(BookmarksTree newBookmarksTree) {
		BookmarksTreeMerger bookmarksTreeMerger = new BookmarksTreeMerger(newBookmarksTree);
		bookmarksTreeMerger.merge(bookmarksTreeModifier);
	}

}
