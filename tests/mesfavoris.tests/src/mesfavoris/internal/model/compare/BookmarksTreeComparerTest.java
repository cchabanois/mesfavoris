package mesfavoris.internal.model.compare;

import static mesfavoris.tests.commons.bookmarks.BookmarksTreeTestUtil.getBookmark;
import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.Test;

import mesfavoris.internal.model.compare.BookmarksTreeComparer;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.model.modification.BookmarkPropertiesModification;
import mesfavoris.tests.commons.bookmarks.BookmarksTreeGenerator;
import mesfavoris.tests.commons.bookmarks.IncrementalIDGenerator;

public class BookmarksTreeComparerTest {

	@Test
	public void testSameProperties() {
		// Given
		BookmarksTree sourceTree = new BookmarksTreeGenerator(new IncrementalIDGenerator(), 5, 3, 2).build();
		BookmarksTree targetTree = new BookmarksTreeGenerator(new IncrementalIDGenerator(), 5, 3, 2).build();

		// When
		BookmarksTreeComparer comparer = new BookmarksTreeComparer(sourceTree, targetTree);
		Optional<BookmarkPropertiesModification> optionalModification = comparer
				.compareBookmarkProperties(getBookmark(sourceTree, 1, 1, 1).getId());

		// Then
		assertFalse(optionalModification.isPresent());
	}

	@Test
	public void testDifferentProperties() {
		// Given
		BookmarksTree sourceTree = new BookmarksTreeGenerator(new IncrementalIDGenerator(), 5, 3, 2).build();
		BookmarkId bookmarkId = getBookmark(sourceTree, 1, 1, 1).getId();
		BookmarksTree targetTree = new BookmarksTreeGenerator(new IncrementalIDGenerator(), 5, 3, 2).build()
				.setPropertyValue(bookmarkId, "newProperty", "value");

		// When
		BookmarksTreeComparer comparer = new BookmarksTreeComparer(sourceTree, targetTree);
		Optional<BookmarkPropertiesModification> optionalModification = comparer
				.compareBookmarkProperties(bookmarkId);

		// Then
		assertTrue(optionalModification.isPresent());
	}

}
