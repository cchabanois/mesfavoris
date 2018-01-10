package mesfavoris.internal.model.merge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import mesfavoris.internal.model.replay.ModificationsReplayer;
import mesfavoris.model.BookmarkId;
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
			bookmarksTree = randomModificationApplier.applyRandomModification(bookmarksTree,
					new PrintWriter(stringWriter));
		}

		// Then
		BookmarksTreeModifier bookmarksTreeModifier = new BookmarksTreeModifier(originalBookmarksTree);
		merge(bookmarksTree, bookmarksTreeModifier);
		assertEquals(bookmarksTree.toString(), bookmarksTreeModifier.getCurrentTree().toString());
	}

	@Test
	public void testMergeRandomModificationsThenReplayOnTarget() {
		// Given
		int numModifications = 100;
		BookmarksTree bookmarksTree = originalBookmarksTree;
		BookmarksTreeModifier bookmarksTreeModifier0 = new BookmarksTreeModifier(bookmarksTree);
		applyRandomModification(bookmarksTreeModifier0, numModifications);

		bookmarksTree = bookmarksTreeModifier0.getCurrentTree();
		BookmarksTreeModifier bookmarksTreeModifier1 = new BookmarksTreeModifier(originalBookmarksTree);
		merge(bookmarksTree, bookmarksTreeModifier1);

		// When
		ModificationsReplayer replayer = new ModificationsReplayer(bookmarksTreeModifier1.getModifications());
		BookmarksTreeModifier bookmarksTreeModifier2 = new BookmarksTreeModifier(bookmarksTree);
		replayer.replayModifications(bookmarksTreeModifier2);
		bookmarksTreeModifier2.optimize();

		// Then
		assertEquals(bookmarksTree.toString(), bookmarksTreeModifier2.getCurrentTree().toString());
		assertSame(bookmarksTree, bookmarksTreeModifier2.getCurrentTree());
	}

	/**
	 * When we replay the modifications on the target bookmarksTree, the
	 * bookmarksTree is not supposed to be changed. But if we don't call optimize(),
	 * there may be unneeded changes (they cancel each other out).
	 */	
	@Test
	public void testMergeModificationsThenReplayOnTargetNeedsOptimize() {
		BookmarksTree bookmarksTree = originalBookmarksTree;
		BookmarksTreeModifier bookmarksTreeModifier0 = new BookmarksTreeModifier(bookmarksTree);
		bookmarksTreeModifier0.moveBefore(Arrays.asList(new BookmarkId("87")), new BookmarkId("82"),
				new BookmarkId("86"));
		bookmarksTreeModifier0.moveAfter(Arrays.asList(new BookmarkId("83")), new BookmarkId("82"),
				new BookmarkId("86"));

		bookmarksTree = bookmarksTreeModifier0.getCurrentTree();
		BookmarksTreeModifier bookmarksTreeModifier1 = new BookmarksTreeModifier(originalBookmarksTree);
		merge(bookmarksTree, bookmarksTreeModifier1);

		// When
		ModificationsReplayer replayer = new ModificationsReplayer(bookmarksTreeModifier1.getModifications());
		BookmarksTreeModifier bookmarksTreeModifier2 = new BookmarksTreeModifier(bookmarksTree);
		replayer.replayModifications(bookmarksTreeModifier2);

		// Then
		assertEquals(bookmarksTree.toString(), bookmarksTreeModifier2.getCurrentTree().toString());
		assertNotSame(bookmarksTree, bookmarksTreeModifier2.getCurrentTree());
		bookmarksTreeModifier2.optimize();
		assertSame(bookmarksTree, bookmarksTreeModifier2.getCurrentTree());
	}

	private void merge(BookmarksTree newBookmarksTree, BookmarksTreeModifier bookmarksTreeModifier) {
		BookmarksTreeMerger bookmarksTreeMerger = new BookmarksTreeMerger(newBookmarksTree);
		bookmarksTreeMerger.merge(bookmarksTreeModifier);
	}

	private void applyRandomModification(BookmarksTreeModifier bookmarksTreeModifier, int numModifications) {
		for (int i = 0; i < numModifications; i++) {
			StringWriter stringWriter = new StringWriter();
			randomModificationApplier.applyRandomModification(bookmarksTreeModifier, new PrintWriter(stringWriter));
		}
	}

}
