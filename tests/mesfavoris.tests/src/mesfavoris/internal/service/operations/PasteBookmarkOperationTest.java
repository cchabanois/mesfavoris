package mesfavoris.internal.service.operations;

import static mesfavoris.tests.commons.bookmarks.BookmarksTreeTestUtil.getBookmark;
import static mesfavoris.tests.commons.bookmarks.BookmarksTreeTestUtil.getBookmarkFolder;
import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.URLTransfer;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.ui.IWorkbenchPart;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import mesfavoris.BookmarksException;
import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.tests.commons.bookmarks.BookmarksTreeGenerator;
import mesfavoris.tests.commons.bookmarks.IncrementalIDGenerator;

public class PasteBookmarkOperationTest {
	private BookmarkDatabase bookmarkDatabase;
	private PasteBookmarkOperation pasteBookmarkOperation;
	private IBookmarkPropertiesProvider bookmarkPropertiesProvider = new TestBookmarkPropertiesProvider();

	@Before
	public void setUp() {
		BookmarksTree bookmarksTree = new BookmarksTreeGenerator(new IncrementalIDGenerator(), 5, 3, 2).build();
		bookmarkDatabase = new BookmarkDatabase("main", bookmarksTree);
		pasteBookmarkOperation = new PasteBookmarkOperation(bookmarkDatabase, bookmarkPropertiesProvider);
	}

	@Test
	public void testPasteBookmarks() throws Exception {
		// Given
		copyToClipboard(getBookmarkFolder(bookmarkDatabase.getBookmarksTree(), 1, 1, 1).getId(),
				getBookmark(bookmarkDatabase.getBookmarksTree(), 2, 2, 2, 2).getId());
		int numberOfBookmarksBefore = bookmarkDatabase.getBookmarksTree().size();

		// When
		pasteBookmarkOperation.paste(getBookmarkFolder(bookmarkDatabase.getBookmarksTree(), 0, 0, 0).getId(),
				new NullProgressMonitor());

		// Then
		assertEquals(numberOfBookmarksBefore + 7, bookmarkDatabase.getBookmarksTree().size());

	}

	@Test
	public void testPasteInvalidString() throws BookmarksException {
		// Given
		copyToClipboard("Not a bookmark", TextTransfer.getInstance());
		BookmarksTree previousTree = bookmarkDatabase.getBookmarksTree();

		// When
		pasteBookmarkOperation.paste(getBookmarkFolder(bookmarkDatabase.getBookmarksTree(), 0, 0, 0).getId(),
				new NullProgressMonitor());

		// Then
		assertEquals(previousTree, bookmarkDatabase.getBookmarksTree());
	}

	@Test
	public void testPasteUrlAsText() throws Exception {
		// Given
		// on mac, when you copy from firefox
		copyToClipboard("http://www.google.com", TextTransfer.getInstance());
		int numberOfBookmarksBefore = bookmarkDatabase.getBookmarksTree().size();

		// When
		pasteBookmarkOperation.paste(getBookmarkFolder(bookmarkDatabase.getBookmarksTree(), 0, 0, 0).getId(),
				new NullProgressMonitor());

		// Then
		assertEquals(numberOfBookmarksBefore + 1, bookmarkDatabase.getBookmarksTree().size());
	}

	@Test
	public void testPasteUrl() throws BookmarksException {
		// Given
		// on mac, when you copy from chrome
		copyToClipboard("http://www.google.com", URLTransfer.getInstance());
		int numberOfBookmarksBefore = bookmarkDatabase.getBookmarksTree().size();

		// When
		pasteBookmarkOperation.paste(getBookmarkFolder(bookmarkDatabase.getBookmarksTree(), 0, 0, 0).getId(),
				new NullProgressMonitor());

		// Then
		assertEquals(numberOfBookmarksBefore + 1, bookmarkDatabase.getBookmarksTree().size());		
	}
	
	private void copyToClipboard(BookmarkId... bookmarkIds) {
		CopyBookmarkOperation copyBookmarkOperation = new CopyBookmarkOperation();
		copyBookmarkOperation.copyToClipboard(bookmarkDatabase.getBookmarksTree(), Lists.newArrayList(bookmarkIds));
	}

	public void copyToClipboard(Object data, Transfer transfer) {
		UIThreadRunnable.syncExec(() -> {
			Clipboard clipboard = new Clipboard(null);
			try {
				Transfer[] transfers = new Transfer[] { transfer };
				Object[] contents = new Object[] { data };
				clipboard.setContents(contents, transfers);
			} finally {
				clipboard.dispose();
			}
		});
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
