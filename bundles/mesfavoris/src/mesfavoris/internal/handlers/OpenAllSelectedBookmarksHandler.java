package mesfavoris.internal.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.statushandlers.StatusManager;

import mesfavoris.BookmarksException;
import mesfavoris.MesFavoris;
import mesfavoris.commons.core.AdapterUtils;
import mesfavoris.handlers.AbstractBookmarkHandler;
import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.StatusHelper;
import mesfavoris.internal.model.merge.BookmarksTreeIterable;
import mesfavoris.internal.model.merge.BookmarksTreeIterable.Algorithm;
import mesfavoris.internal.views.virtual.BookmarkLink;
import mesfavoris.internal.views.virtual.VirtualBookmarkFolder;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;

public class OpenAllSelectedBookmarksHandler extends AbstractBookmarkHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		BookmarksTree bookmarksTree = MesFavoris.getBookmarkDatabase().getBookmarksTree();
		Set<Bookmark> bookmarks = getAllSelectedBookmarks(bookmarksTree, selection);
		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		try {
			progressService.busyCursorWhile(monitor -> {
				List<IStatus> statii = new ArrayList<>();
				SubMonitor subMonitor = SubMonitor.convert(monitor, bookmarks.size());
				for (Bookmark bookmark : bookmarks) {
					try {
						bookmarksService.gotoBookmark(bookmark.getId(), subMonitor.newChild(1));
					} catch (BookmarksException e) {
						statii.add(new Status(IStatus.ERROR, BookmarksPlugin.PLUGIN_ID, Status.OK,
								String.format("Could not go to bookmark named '%s'",
										bookmark.getPropertyValue(Bookmark.PROPERTY_NAME)),
								e));
					}
				}
				if (statii.size() > 0) {
					MultiStatus multiStatus = new MultiStatus(BookmarksPlugin.PLUGIN_ID, Status.OK,
							statii.toArray(new IStatus[statii.size()]), "Could not open some bookmarks", null);

					throw new InvocationTargetException(new BookmarksException(multiStatus));
				}
			});
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof BookmarksException) {
				BookmarksException bookmarksException = (BookmarksException) e.getCause();
				StatusManager.getManager().handle(bookmarksException.getStatus(), StatusManager.SHOW);
			} else {
				StatusHelper.showError("Could not go to bookmarks", e.getCause(), false);
			}
		} catch (InterruptedException e) {
			throw new ExecutionException("Could not go to bookmarks : cancelled");
		}
		return null;
	}

	private Set<Bookmark> getAllSelectedBookmarks(BookmarksTree bookmarksTree, IStructuredSelection selection) {
		Set<Bookmark> bookmarkIds = new LinkedHashSet<>();
		for (Object element : selection.toList()) {
			if (element instanceof VirtualBookmarkFolder) {
				VirtualBookmarkFolder virtualBookmarkFolder = (VirtualBookmarkFolder) element;
				for (BookmarkLink bookmarkLink : virtualBookmarkFolder.getChildren()) {
					Bookmark bookmark = AdapterUtils.getAdapter(bookmarkLink, Bookmark.class);
					if (bookmark instanceof BookmarkFolder) {
						bookmarkIds.addAll(getAllNonFolderBookmarks(bookmarksTree, bookmark.getId()));
					} else if (bookmark != null) {
						bookmarkIds.add(bookmark);
					}
				}
			} else {
				Bookmark bookmark = AdapterUtils.getAdapter(element, Bookmark.class);
				if (bookmark instanceof BookmarkFolder) {
					bookmarkIds.addAll(getAllNonFolderBookmarks(bookmarksTree, bookmark.getId()));
				} else if (bookmark != null) {
					bookmarkIds.add(bookmark);
				}
			}
		}
		return bookmarkIds;
	}

	private List<Bookmark> getAllNonFolderBookmarks(BookmarksTree bookmarksTree, BookmarkId folderId) {
		BookmarksTreeIterable bookmarksTreeIterable = new BookmarksTreeIterable(bookmarksTree, folderId,
				Algorithm.PRE_ORDER, b -> !(b instanceof BookmarkFolder));
		return StreamSupport.stream(bookmarksTreeIterable.spliterator(), false).collect(Collectors.toList());
	}

}
