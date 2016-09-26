package mesfavoris.internal.service.operations;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.junit.Before;
import org.junit.Test;

import static mesfavoris.tests.commons.bookmarks.BookmarksTreeTestUtil.getBookmark;
import static mesfavoris.tests.commons.bookmarks.BookmarksTreeTestUtil.getBookmarkFolder;
import static org.junit.Assert.*;

import com.google.common.collect.Lists;

import mesfavoris.internal.service.operations.CopyBookmarkOperation;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.persistence.json.BookmarksTreeJsonDeserializer;
import mesfavoris.tests.commons.bookmarks.BookmarksTreeGenerator;
import mesfavoris.tests.commons.bookmarks.IncrementalIDGenerator;

public class CopyBookmarkOperationTest {
	private CopyBookmarkOperation copyBookmarkOperation = new CopyBookmarkOperation();
	private BookmarksTree bookmarksTree;

	@Before
	public void setUp() {
		bookmarksTree = new BookmarksTreeGenerator(new IncrementalIDGenerator(), 5, 3, 2).build();
	}

	@Test
	public void testCopyBookmarksToClipboard() throws Exception {
		// Given
		List<BookmarkId> selection = Lists.newArrayList(getBookmarkFolder(bookmarksTree, 1, 1, 1).getId(),
				getBookmark(bookmarksTree, 2, 2, 2, 2).getId());

		// When
		copyBookmarkOperation.copyToClipboard(bookmarksTree, selection);
		String clipbpardContents = getClipboardContents();

		// Then
		BookmarksTree bookmarksTreeClipboard = deserialize(clipbpardContents);
		assertEquals(2, bookmarksTreeClipboard.getChildren(bookmarksTreeClipboard.getRootFolder().getId()).size());
	}

	private String getClipboardContents() {
		Clipboard clipboard = new Clipboard(null);
		String textData;
		try {
			TextTransfer textTransfer = TextTransfer.getInstance();
			textData = (String) clipboard.getContents(textTransfer);
		} finally {
			clipboard.dispose();
		}
		return textData;
	}

	private BookmarksTree deserialize(String serializedBookmarksTree) throws IOException {
		BookmarksTreeJsonDeserializer deserializer = new BookmarksTreeJsonDeserializer();
		return deserializer.deserialize(new StringReader(serializedBookmarksTree), new NullProgressMonitor());
	}

}
