package mesfavoris.internal.model.replay;

import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import mesfavoris.internal.model.replay.ModificationsReplayer;
import mesfavoris.model.BookmarksTree;
import mesfavoris.model.modification.BookmarksModification;
import mesfavoris.model.modification.BookmarksTreeModifier;
import mesfavoris.tests.commons.bookmarks.BookmarksTreeGenerator;
import mesfavoris.tests.commons.bookmarks.IDGenerator;
import mesfavoris.tests.commons.bookmarks.IncrementalIDGenerator;
import mesfavoris.tests.commons.bookmarks.RandomModificationApplier;

public class ModificationsReplayerTest {
	private BookmarksTree originalBookmarksTree;
	private final IDGenerator idGenerator = new IncrementalIDGenerator();
	private RandomModificationApplier randomModificationApplier;

	@Before
	public void setUp() {
		originalBookmarksTree = new BookmarksTreeGenerator(idGenerator, 5, 3, 2).build();
		randomModificationApplier = new RandomModificationApplier(idGenerator);
	}

	@Test
	public void testReplayOnOriginalBookmarksTree() {
		// Given
		BookmarksTreeModifier bookmarksTreeModifier1 = new BookmarksTreeModifier(originalBookmarksTree);
		applyRandomModifications(bookmarksTreeModifier1, 2000);

		// When
		ModificationsReplayer replayer = new ModificationsReplayer(bookmarksTreeModifier1.getModifications());
		BookmarksTreeModifier bookmarksTreeModifier2 = new BookmarksTreeModifier(originalBookmarksTree);
		List<BookmarksModification> modificationsNotReplayed = replayer.replayModifications(bookmarksTreeModifier2);

		// Then
		assertEquals(0, modificationsNotReplayed.size());
		assertEquals(bookmarksTreeModifier2.getCurrentTree().toString(),
				bookmarksTreeModifier1.getCurrentTree().toString());
	}
	
	@Test
	public void testCannotReplayAllModificationsOnModifiedBookmarksTree() {
		// Given
		BookmarksTreeModifier bookmarksTreeModifier1 = new BookmarksTreeModifier(originalBookmarksTree);
		applyRandomModifications(bookmarksTreeModifier1, 200);
		BookmarksTreeModifier bookmarksTreeModifier2 = new BookmarksTreeModifier(originalBookmarksTree);
		applyRandomModifications(bookmarksTreeModifier2, 200);
		
		// When
		ModificationsReplayer replayer = new ModificationsReplayer(bookmarksTreeModifier1.getModifications());
		BookmarksTreeModifier bookmarksTreeModifier3 = new BookmarksTreeModifier(bookmarksTreeModifier2.getCurrentTree());
		List<BookmarksModification> modificationsNotReplayed = replayer.replayModifications(bookmarksTreeModifier3);

		// Then
		assertNotEquals(0, modificationsNotReplayed.size());
	}

	private void applyRandomModifications(BookmarksTreeModifier bookmarksTreeModifier, int numModifications) {
		StringWriter stringWriter = new StringWriter();
		for (int i = 0; i < numModifications; i++) {
			randomModificationApplier.applyRandomModification(bookmarksTreeModifier, new PrintWriter(stringWriter));
		}
	}

}
