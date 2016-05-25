package mesfavoris.internal.model.compare;

import static mesfavoris.testutils.BookmarksTreeTestUtil.getBookmark;
import static org.junit.Assert.*;

import java.util.Optional;

import org.chabanois.mesfavoris.internal.model.compare.BookmarksTreeComparer;
import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.model.BookmarksTree;
import org.chabanois.mesfavoris.model.modification.BookmarkPropertiesModification;
import org.junit.Test;

import mesfavoris.testutils.BookmarksTreeBuilder;
import mesfavoris.testutils.IncrementalIDGenerator;

public class BookmarksTreeComparerTest {

	@Test
	public void testSameProperties() {
		// Given
		BookmarksTree sourceTree = new BookmarksTreeBuilder(new IncrementalIDGenerator(), 5, 3, 2).build();
		BookmarksTree targetTree = new BookmarksTreeBuilder(new IncrementalIDGenerator(), 5, 3, 2).build();

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
		BookmarksTree sourceTree = new BookmarksTreeBuilder(new IncrementalIDGenerator(), 5, 3, 2).build();
		BookmarkId bookmarkId = getBookmark(sourceTree, 1, 1, 1).getId();
		BookmarksTree targetTree = new BookmarksTreeBuilder(new IncrementalIDGenerator(), 5, 3, 2).build()
				.setPropertyValue(bookmarkId, "newProperty", "value");

		// When
		BookmarksTreeComparer comparer = new BookmarksTreeComparer(sourceTree, targetTree);
		Optional<BookmarkPropertiesModification> optionalModification = comparer
				.compareBookmarkProperties(bookmarkId);

		// Then
		assertTrue(optionalModification.isPresent());
	}

}
