package mesfavoris.internal.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressService;

import mesfavoris.MesFavoris;
import mesfavoris.handlers.AbstractBookmarkHandler;
import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.StatusHelper;
import mesfavoris.markers.IBookmarksMarkers;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;

public class DeleteBookmarkMarkerHandler extends AbstractBookmarkHandler {
	private final BookmarkDatabase bookmarkDatabase;
	private final IBookmarksMarkers bookmarksMarkers;

	public DeleteBookmarkMarkerHandler() {
		this.bookmarkDatabase = MesFavoris.getBookmarkDatabase();
		this.bookmarksMarkers = BookmarksPlugin.getDefault().getBookmarksMarkers();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		if (selection.isEmpty()) {
			return null;
		}

		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		try {
			progressService.busyCursorWhile(monitor -> {
				Set<Bookmark> bookmarks = getSelectedBookmarksRecursively(bookmarkDatabase.getBookmarksTree(),
						selection);
				SubMonitor subMonitor = SubMonitor.convert(monitor, "Delete bookmark markers", bookmarks.size());
				for (Bookmark bookmark : bookmarks) {
					bookmarksMarkers.deleteMarker(bookmark.getId(), subMonitor.newChild(1));
				}
			});
		} catch (InvocationTargetException e) {
			StatusHelper.showError("Could not delete bookmark marker", e.getCause(), false);
		} catch (InterruptedException e) {
			throw new ExecutionException("Could not delete bookmark marker : cancelled");
		}
		return null;
	}

	private Set<Bookmark> getSelectedBookmarksRecursively(BookmarksTree bookmarksTree, IStructuredSelection selection) {
		Set<Bookmark> bookmarks = new LinkedHashSet<>();
		for (Bookmark bookmark : ((List<Bookmark>) (selection.toList()))) {
			bookmarks.add(bookmark);
			if (bookmark instanceof BookmarkFolder) {
				getAllBookmarksUnder(bookmarksTree, bookmark.getId(), bookmarks);
			}
		}
		return bookmarks;
	}

	private void getAllBookmarksUnder(BookmarksTree bookmarksTree, BookmarkId bookmarkFolderId,
			Set<Bookmark> bookmarks) {
		List<Bookmark> children = bookmarksTree.getChildren(bookmarkFolderId);
		bookmarks.addAll(children);
		for (Bookmark bookmark : children) {
			if (bookmark instanceof BookmarkFolder) {
				getAllBookmarksUnder(bookmarksTree, bookmark.getId(), bookmarks);
			}
		}
	}

}
