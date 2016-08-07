package mesfavoris.internal.operations;

import static mesfavoris.testutils.BookmarksTreeTestUtil.getBookmark;
import static mesfavoris.testutils.BookmarksTreeTestUtil.getBookmarkFolder;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.collect.Lists;

import mesfavoris.BookmarksException;
import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.testutils.BookmarksTreeBuilder;
import mesfavoris.testutils.IncrementalIDGenerator;
import mesfavoris.validation.IBookmarkModificationValidator;

public class PasteBookmarkOperationTest {
	private BookmarkDatabase bookmarkDatabase;
	private PasteBookmarkOperation pasteBookmarkOperation;
	private IBookmarkModificationValidator bookmarkModificationValidator = mock(IBookmarkModificationValidator.class);
	private IBookmarkPropertiesProvider bookmarkPropertiesProvider = new TestBookmarkPropertiesProvider();

	@Before
	public void setUp() {
		BookmarksTree bookmarksTree = new BookmarksTreeBuilder(new IncrementalIDGenerator(), 5, 3, 2).build();
		bookmarkDatabase = new BookmarkDatabase("main", bookmarksTree);
		pasteBookmarkOperation = new PasteBookmarkOperation(bookmarkDatabase, bookmarkPropertiesProvider,
				bookmarkModificationValidator);
		when(bookmarkModificationValidator.validateModification(any(BookmarksTree.class), any(BookmarkId.class)))
				.thenReturn(Status.OK_STATUS);
	}

	@Test
	public void testPasteBookmarks() throws Exception {
		// Given
		copyToClipboard(getBookmarkFolder(bookmarkDatabase.getBookmarksTree(), 1, 1, 1).getId(),
				getBookmark(bookmarkDatabase.getBookmarksTree(), 2, 2, 2, 2).getId());
		int numberOfBookmarksBefore = bookmarkDatabase.getBookmarksTree().size();

		// When
		pasteBookmarkOperation.paste(Display.getCurrent(),
				getBookmarkFolder(bookmarkDatabase.getBookmarksTree(), 0, 0, 0).getId(), new NullProgressMonitor());

		// Then
		assertEquals(numberOfBookmarksBefore + 7, bookmarkDatabase.getBookmarksTree().size());

	}

	@Test
	public void testPasteInvalidString() throws BookmarksException {
		// Given
		copyToClipboard("Not a bookmark");
		BookmarksTree previousTree = bookmarkDatabase.getBookmarksTree();

		// When
		pasteBookmarkOperation.paste(Display.getCurrent(),
				getBookmarkFolder(bookmarkDatabase.getBookmarksTree(), 0, 0, 0).getId(), new NullProgressMonitor());

		// Then
		assertEquals(previousTree, bookmarkDatabase.getBookmarksTree());
	}

	@Test
	public void testPasteUrl() throws Exception {
		// Given
		copyToClipboard("http://www.google.com");
		int numberOfBookmarksBefore = bookmarkDatabase.getBookmarksTree().size();

		// When
		pasteBookmarkOperation.paste(Display.getCurrent(),
				getBookmarkFolder(bookmarkDatabase.getBookmarksTree(), 0, 0, 0).getId(), new NullProgressMonitor());

		// Then
		assertEquals(numberOfBookmarksBefore + 1, bookmarkDatabase.getBookmarksTree().size());
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

	private static class TestBookmarkPropertiesProvider implements IBookmarkPropertiesProvider {

		@Override
		public void addBookmarkProperties(Map<String, String> bookmarkProperties, IWorkbenchPart part,
				ISelection selection, IProgressMonitor monitor) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object firstElement = structuredSelection.getFirstElement();
			if (firstElement == null) {
				return;
			}
			if (!(firstElement instanceof URL)) {
				return;
			}
			URL url = (URL) firstElement;
			bookmarkProperties.put(Bookmark.PROPERTY_NAME, url.toString());
		}

	}

}
