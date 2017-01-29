package mesfavoris.internal.service.operations;

import java.util.Collections;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.google.common.collect.ImmutableMap;

import mesfavoris.BookmarksException;
import mesfavoris.bookmarktype.IBookmarkLocation;
import mesfavoris.bookmarktype.IBookmarkLocationProvider;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.markers.IBookmarksMarkers;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.problems.BookmarkProblem;
import mesfavoris.problems.IBookmarkProblems;
import mesfavoris.problems.BookmarkProblem.Severity;
import mesfavoris.topics.BookmarksEvents;

public class GotoBookmarkOperation {
	private final BookmarkDatabase bookmarkDatabase;
	private final IBookmarkLocationProvider bookmarkLocationProvider;
	private final IGotoBookmark gotoBookmark;
	private final IBookmarksMarkers bookmarksMarkers;
	private final IEventBroker eventBroker;
	private final IBookmarkProblems bookmarkProblems;

	public GotoBookmarkOperation(BookmarkDatabase bookmarkDatabase, IBookmarkLocationProvider bookmarkLocationProvider,
			IGotoBookmark gotoBookmark, IBookmarksMarkers bookmarksMarkers, IBookmarkProblems bookmarkProblems,
			IEventBroker eventBroker) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.bookmarkLocationProvider = bookmarkLocationProvider;
		this.gotoBookmark = gotoBookmark;
		this.bookmarksMarkers = bookmarksMarkers;
		this.bookmarkProblems = bookmarkProblems;
		this.eventBroker = eventBroker;
	}

	public void gotoBookmark(BookmarkId bookmarkId, IProgressMonitor monitor) throws BookmarksException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Goto bookmark", 100);
		Bookmark bookmark = bookmarkDatabase.getBookmarksTree().getBookmark(bookmarkId);
		IBookmarkLocation bookmarkLocation = bookmarkLocationProvider.getBookmarkLocation(bookmark,
				subMonitor.newChild(100));
		if (bookmarkLocation == null) {
			addGotoBookmarkProblem(bookmarkId);
			throw new BookmarksException("Could not find bookmark");
		}
		Display.getDefault().asyncExec(() -> {
			IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (gotoBookmark.gotoBookmark(workbenchWindow, bookmark, bookmarkLocation)) {
				removeGotoBookmarkProblem(bookmarkId);
				refreshMarker(bookmark);
				postBookmarkVisited(bookmarkId);
			} else {
				addGotoBookmarkProblem(bookmarkId);
			}
		});
	}

	private void removeGotoBookmarkProblem(BookmarkId bookmarkId) {
		bookmarkProblems.getBookmarkProblem(bookmarkId, BookmarkProblem.TYPE_CANNOT_GOTOBOOKMARK)
				.ifPresent(problem -> bookmarkProblems.delete(problem));
	}

	private void addGotoBookmarkProblem(BookmarkId bookmarkId) {
		BookmarkProblem problem = new BookmarkProblem(bookmarkId, BookmarkProblem.TYPE_CANNOT_GOTOBOOKMARK,
				Severity.ERROR, Collections.emptyMap());
		bookmarkProblems.add(problem);
	}

	private void postBookmarkVisited(BookmarkId bookmarkId) {
		eventBroker.post(BookmarksEvents.TOPIC_BOOKMARK_VISITED, ImmutableMap.of("bookmarkId", bookmarkId));
	}

	private void refreshMarker(Bookmark bookmark) {
		new Job("Refreshing marker") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				bookmarksMarkers.refreshMarker(bookmark.getId(), monitor);
				return Status.OK_STATUS;
			}
		}.schedule();
	}

}
