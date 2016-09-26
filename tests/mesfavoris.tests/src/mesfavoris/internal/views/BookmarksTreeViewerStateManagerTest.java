package mesfavoris.internal.views;

import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmark;
import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmarkFolder;
import static mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder.bookmarksTree;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;
import org.junit.Before;
import org.junit.Test;

import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder;

public class BookmarksTreeViewerStateManagerTest {
	private BookmarksTreeViewerStateManager stateManager;
	private BookmarksTreeViewer bookmarksTreeViewer = mock(BookmarksTreeViewer.class);
	private IMemento memento;
	private BookmarkDatabase bookmarkDatabase;

	@Before
	public void setUp() {
		bookmarkDatabase = new BookmarkDatabase("test", getInitialTree());
		memento = XMLMemento.createWriteRoot("bookmarksTree");
		stateManager = new BookmarksTreeViewerStateManager(bookmarksTreeViewer);
		when(bookmarksTreeViewer.getBookmarkDatabase()).thenReturn(bookmarkDatabase);
	}

	@Test
	public void testSaveState() {
		// Given
		when(bookmarksTreeViewer.getSelectedBookmark()).thenReturn(getBookmark("bookmark1"));
		when(bookmarksTreeViewer.getVisibleExpandedElements()).thenReturn(new Object[] { getBookmark("rootFolder"),
				getBookmark("bookmarkFolder2"), getBookmark("bookmarkFolder3") });

		// When
		stateManager.saveState(memento);

		// Then
		assertEquals("rootFolder,bookmarkFolder2,bookmarkFolder3",
				memento.getString(BookmarksTreeViewerStateManager.STORE_EXPANDED));
		assertEquals("bookmark1", memento.getString(BookmarksTreeViewerStateManager.STORE_SELECTED));
	}

	@Test
	public void testRestoreState() {
		// Given
		when(bookmarksTreeViewer.getSelectedBookmark()).thenReturn(getBookmark("bookmark1"));
		when(bookmarksTreeViewer.getVisibleExpandedElements()).thenReturn(new Object[] { getBookmark("rootFolder"),
				getBookmark("bookmarkFolder2"), getBookmark("bookmarkFolder3") });
		stateManager.saveState(memento);

		// When
		stateManager.restoreState(memento);

		// Then
		verify(bookmarksTreeViewer).setSelection(new StructuredSelection(getBookmark("bookmark1")), false);
		verify(bookmarksTreeViewer).setExpandedElements(new Object[] { getBookmark("rootFolder"),
				getBookmark("bookmarkFolder2"), getBookmark("bookmarkFolder3") });
	}

	private BookmarksTree getInitialTree() {
		BookmarksTreeBuilder bookmarksTreeBuilder = bookmarksTree("rootFolder");
		bookmarksTreeBuilder.addBookmarks("rootFolder", bookmarkFolder("bookmarkFolder1"),
				bookmarkFolder("bookmarkFolder2"));
		bookmarksTreeBuilder.addBookmarks("bookmarkFolder1", bookmark("bookmark1"), bookmark("bookmark2"));
		bookmarksTreeBuilder.addBookmarks("bookmarkFolder2", bookmarkFolder("bookmarkFolder3"), bookmark("bookmark3"),
				bookmark("bookmark4"));
		bookmarksTreeBuilder.addBookmarks("bookmarkFolder3", bookmark("bookmark5"));

		return bookmarksTreeBuilder.build();
	}

	private Bookmark getBookmark(String id) {
		return bookmarkDatabase.getBookmarksTree().getBookmark(new BookmarkId(id));
	}

}
