package mesfavoris.internal.model.merge;

import static org.junit.Assert.assertEquals;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;

import mesfavoris.internal.model.merge.BookmarksTreeMerger;
import mesfavoris.model.BookmarksTree;
import mesfavoris.model.modification.BookmarksTreeModifier;
import mesfavoris.tests.commons.bookmarks.BookmarksTreeGenerator;
import mesfavoris.tests.commons.bookmarks.IDGenerator;
import mesfavoris.tests.commons.bookmarks.IncrementalIDGenerator;
import mesfavoris.tests.commons.bookmarks.RandomModificationApplier;

public class BookmarksTreeMergerRandomTest {
	private BookmarksTree originalBookmarksTree;
	private final IDGenerator idGenerator = new IncrementalIDGenerator();
	private RandomModificationApplier randomModificationApplier;
	
	@Before
	public void setUp() {
		originalBookmarksTree = new BookmarksTreeGenerator(idGenerator, 5, 3, 2).build();
		randomModificationApplier = new RandomModificationApplier(idGenerator);
	}

	@Test
	public void testMultipleRandomModifications() {
		// Given
		int numModifications = 2000;
		BookmarksTree bookmarksTree = originalBookmarksTree;
		StringWriter stringWriter = new StringWriter();
		
		// When
		for (int i = 0; i < numModifications; i++) {
			bookmarksTree = randomModificationApplier.applyRandomModification(bookmarksTree, new PrintWriter(stringWriter));
		}
		
		// Then
		BookmarksTreeModifier bookmarksTreeModifier = new BookmarksTreeModifier(originalBookmarksTree);
		merge(bookmarksTree, bookmarksTreeModifier);
		assertEquals(bookmarksTree.toString(), bookmarksTreeModifier.getCurrentTree().toString());
	}

	private void merge(BookmarksTree newBookmarksTree, BookmarksTreeModifier bookmarksTreeModifier) {
		BookmarksTreeMerger bookmarksTreeMerger = new BookmarksTreeMerger(newBookmarksTree);
		bookmarksTreeMerger.merge(bookmarksTreeModifier);
	}

}
