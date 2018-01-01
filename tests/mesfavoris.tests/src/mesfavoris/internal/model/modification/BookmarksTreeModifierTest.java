package mesfavoris.internal.model.modification;

import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmark;
import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmarkFolder;
import static mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder.bookmarksTree;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.model.modification.BookmarksAddedModification;
import mesfavoris.model.modification.BookmarksMovedModification;
import mesfavoris.model.modification.BookmarksTreeModifier;
import mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder;

public class BookmarksTreeModifierTest {
	private BookmarksTreeModifier bookmarksTreeModifier;

	@Before
	public void setUp() {
		bookmarksTreeModifier = new BookmarksTreeModifier(createBookmarksTree());
	}

	@Test
	public void testAddAfter() {
		// When
		bookmarksTreeModifier.addBookmarksAfter(bookmarkId("root"), bookmarkId("folder1"),
				Arrays.asList(bookmark("bookmarkAdded1").build(), bookmark("bookmarkAdded2").build()));

		// Then
		assertThat(bookmarksTreeModifier.getModifications()).hasSize(1);
		BookmarksAddedModification modification = (BookmarksAddedModification) bookmarksTreeModifier.getModifications()
				.get(0);
		assertThat(modification.getAfterBookmarkId()).isEqualTo(bookmarkId("folder1"));
		assertThat(modification.getBeforeBookmarkId()).isEqualTo(bookmarkId("folder2"));
	}

	@Test
	public void testAddAfterNull() {
		// When
		bookmarksTreeModifier.addBookmarksAfter(bookmarkId("root"), null,
				Arrays.asList(bookmark("bookmarkAdded1").build(), bookmark("bookmarkAdded2").build()));

		// Then
		assertThat(bookmarksTreeModifier.getModifications()).hasSize(1);
		BookmarksAddedModification modification = (BookmarksAddedModification) bookmarksTreeModifier.getModifications()
				.get(0);
		assertThat(modification.getAfterBookmarkId()).isNull();
		assertThat(modification.getBeforeBookmarkId()).isEqualTo(bookmarkId("folder1"));
	}	
	
	@Test
	public void testMoveAfter() {
		// When
		bookmarksTreeModifier.moveAfter(Arrays.asList(bookmarkId("folder11"), bookmarkId("bookmark11")),
				bookmarkId("root"), bookmarkId("folder1"));

		// Then
		assertThat(bookmarksTreeModifier.getModifications()).hasSize(1);
		BookmarksMovedModification modification = (BookmarksMovedModification) bookmarksTreeModifier.getModifications()
				.get(0);
		assertThat(modification.getAfterBookmarkId()).isEqualTo(bookmarkId("folder1"));
		assertThat(modification.getBeforeBookmarkId()).isEqualTo(bookmarkId("folder2"));
	}

	@Test
	public void testMoveAfterNull() {
		// When
		bookmarksTreeModifier.moveAfter(Arrays.asList(bookmarkId("folder11"), bookmarkId("bookmark11")),
				bookmarkId("root"), null);

		// Then
		assertThat(bookmarksTreeModifier.getModifications()).hasSize(1);
		BookmarksMovedModification modification = (BookmarksMovedModification) bookmarksTreeModifier.getModifications()
				.get(0);
		assertThat(modification.getAfterBookmarkId()).isNull();
		assertThat(modification.getBeforeBookmarkId()).isEqualTo(bookmarkId("folder1"));
	}

	@Test
	public void testMoveBefore() {
		// When
		bookmarksTreeModifier.moveBefore(Arrays.asList(bookmarkId("folder11"), bookmarkId("bookmark11")),
				bookmarkId("root"), bookmarkId("folder2"));

		// Then
		assertThat(bookmarksTreeModifier.getModifications()).hasSize(1);
		BookmarksMovedModification modification = (BookmarksMovedModification) bookmarksTreeModifier.getModifications()
				.get(0);
		assertThat(modification.getAfterBookmarkId()).isEqualTo(bookmarkId("folder1"));
		assertThat(modification.getBeforeBookmarkId()).isEqualTo(bookmarkId("folder2"));
	}

	@Test
	public void testMoveBeforeNull() {
		// When
		bookmarksTreeModifier.moveBefore(Arrays.asList(bookmarkId("folder11"), bookmarkId("bookmark11")),
				bookmarkId("root"), null);

		// Then
		assertThat(bookmarksTreeModifier.getModifications()).hasSize(1);
		BookmarksMovedModification modification = (BookmarksMovedModification) bookmarksTreeModifier.getModifications()
				.get(0);
		assertThat(modification.getAfterBookmarkId()).isEqualTo(bookmarkId("folder2"));
		assertThat(modification.getBeforeBookmarkId()).isNull();
	}

	@Test
	public void testMoveNowhere() {
		// When
		bookmarksTreeModifier.moveAfter(Arrays.asList(bookmarkId("folder11"), bookmarkId("bookmark11")),
				bookmarkId("folder1"), null);

		// Then
		assertThat(bookmarksTreeModifier.getModifications()).isEmpty();
	}

	private BookmarksTree createBookmarksTree() {
		BookmarksTreeBuilder bookmarksTreeBuilder = bookmarksTree("root");
		bookmarksTreeBuilder.addBookmarks("root", bookmarkFolder("folder1"), bookmarkFolder("folder2"));
		bookmarksTreeBuilder.addBookmarks("folder1", bookmarkFolder("folder11"), bookmark("bookmark11"));

		return bookmarksTreeBuilder.build();
	}

	private BookmarkId bookmarkId(String id) {
		return new BookmarkId(id);
	}
}
