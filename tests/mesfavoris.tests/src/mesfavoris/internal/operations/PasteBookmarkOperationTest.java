package mesfavoris.internal.operations;

import static mesfavoris.testutils.BookmarksTreeTestUtil.getBookmark;
import static mesfavoris.testutils.BookmarksTreeTestUtil.getBookmarkFolder;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.chabanois.mesfavoris.BookmarksException;
import org.chabanois.mesfavoris.internal.operations.CopyBookmarkOperation;
import org.chabanois.mesfavoris.internal.operations.PasteBookmarkOperation;
import org.chabanois.mesfavoris.model.BookmarkDatabase;
import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.model.BookmarksTree;
import org.chabanois.mesfavoris.validation.IBookmarkModificationValidator;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import mesfavoris.testutils.BookmarksTreeBuilder;
import mesfavoris.testutils.IncrementalIDGenerator;

public class PasteBookmarkOperationTest {
	private BookmarkDatabase bookmarkDatabase;
	private PasteBookmarkOperation pasteBookmarkOperation;
	private IBookmarkModificationValidator bookmarkModificationValidator = mock(IBookmarkModificationValidator.class);

	@Before
	public void setUp() {
		BookmarksTree bookmarksTree = new BookmarksTreeBuilder(new IncrementalIDGenerator(), 5, 3, 2).build();
		bookmarkDatabase = new BookmarkDatabase("main", bookmarksTree);
		pasteBookmarkOperation = new PasteBookmarkOperation(bookmarkDatabase, bookmarkModificationValidator);
		when(bookmarkModificationValidator.validateModification(any(BookmarksTree.class), any(BookmarkId.class)))
				.thenReturn(Status.OK_STATUS);
	}

	@Test
	public void testPaste() throws Exception {
		// Given
		copyToClipboard(getBookmarkFolder(bookmarkDatabase.getBookmarksTree(), 1, 1, 1).getId(),
				getBookmark(bookmarkDatabase.getBookmarksTree(), 2, 2, 2, 2).getId());
		int numberOfBookmarksBefore = bookmarkDatabase.getBookmarksTree().size();

		// When
		pasteBookmarkOperation.paste(getBookmarkFolder(bookmarkDatabase.getBookmarksTree(), 0, 0, 0).getId());

		// Then
		assertEquals(numberOfBookmarksBefore + 7, bookmarkDatabase.getBookmarksTree().size());

	}

	@Test
	public void testPasteWhenClipboardDoesNotContainBookmarksTree() throws BookmarksException {
		// Given
		copyToClipboard("Not a bookmark");
		BookmarksTree previousTree = bookmarkDatabase.getBookmarksTree();

		// When
		pasteBookmarkOperation.paste(getBookmarkFolder(bookmarkDatabase.getBookmarksTree(), 0, 0, 0).getId());

		// Then
		assertEquals(previousTree, bookmarkDatabase.getBookmarksTree());
	}

	private void copyToClipboard(BookmarkId... bookmarkIds) {
		CopyBookmarkOperation copyBookmarkOperation = new CopyBookmarkOperation();
		copyBookmarkOperation.copyToClipboard(bookmarkDatabase.getBookmarksTree(), Lists.newArrayList(bookmarkIds));
	}

	public void copyToClipboard(String text) {
		Clipboard clipboard = new Clipboard(null);
		try {
			TextTransfer textTransfer = TextTransfer.getInstance();
			Transfer[] transfers = new Transfer[] { textTransfer };
			Object[] data = new Object[] { text };
			clipboard.setContents(data, transfers);
		} finally {
			clipboard.dispose();
		}
	}

}
