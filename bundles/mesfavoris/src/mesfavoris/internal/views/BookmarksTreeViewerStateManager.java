package mesfavoris.internal.views;

import java.util.Arrays;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IMemento;

import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;

public class BookmarksTreeViewerStateManager {
	public static final String STORE_SELECTED = "bookmarks.selected";
	public static final String STORE_EXPANDED = "bookmarks.expanded";
	private final BookmarksTreeViewer bookmarksTreeViewer;

	public BookmarksTreeViewerStateManager(BookmarksTreeViewer bookmarksTreeViewer) {
		this.bookmarksTreeViewer = bookmarksTreeViewer;
	}

	public void restoreState(IMemento memento) {
		restoreExpandedElements(memento);
		restoreSelectedElement(memento);
	}

	public void saveState(IMemento memento) {
		saveExpandedElements(memento);
		saveSelectedElement(memento);
	}

	private void restoreExpandedElements(IMemento memento) {
		Object[] expandedElements = loadExpandedElements(bookmarksTreeViewer.getBookmarkDatabase().getBookmarksTree(),
				memento);
		bookmarksTreeViewer.setExpandedElements(expandedElements);
	}

	private void saveExpandedElements(IMemento memento) {
		Object[] expandedElements = bookmarksTreeViewer.getVisibleExpandedElements();
		StringBuilder sb = new StringBuilder();
		for (Object element : expandedElements) {
			if (element instanceof Bookmark) {
				Bookmark bookmark = (Bookmark) element;
				if (sb.length() != 0) {
					sb.append(',');
				}
				sb.append(bookmark.getId());
			}
		}
		memento.putString(STORE_EXPANDED, sb.toString());
	}

	private void saveSelectedElement(IMemento memento) {
		Bookmark bookmark = bookmarksTreeViewer.getSelectedBookmark();
		if (bookmark != null) {
			memento.putString(STORE_SELECTED, bookmark.getId().toString());
		}
	}

	private Object[] loadExpandedElements(BookmarksTree bookmarksTree, IMemento memento) {
		if (memento == null) {
			return new Object[0];
		}
		String expandedElementsStr = memento.getString(STORE_EXPANDED);
		if (expandedElementsStr == null) {
			return new Object[0];
		}
		String[] ids = expandedElementsStr.split(",");
		return Arrays.stream(ids).map(id -> bookmarksTree.getBookmark(new BookmarkId(id)))
				.filter(bookmark -> bookmark != null).toArray(size -> new Object[size]);
	}

	private void restoreSelectedElement(IMemento memento) {
		ISelection selection = loadSelectedElement(bookmarksTreeViewer.getBookmarkDatabase().getBookmarksTree(),
				memento);
		bookmarksTreeViewer.setSelection(selection, false);
	}

	private ISelection loadSelectedElement(BookmarksTree bookmarksTree, IMemento memento) {
		if (memento == null) {
			return new StructuredSelection();
		}
		String selectedIdStr = memento.getString(STORE_SELECTED);
		if (selectedIdStr == null) {
			return new StructuredSelection();
		}
		Bookmark bookmark = bookmarksTree.getBookmark(new BookmarkId(selectedIdStr));
		if (bookmark == null) {
			return new StructuredSelection();
		}
		return new StructuredSelection(bookmark);
	}
}
