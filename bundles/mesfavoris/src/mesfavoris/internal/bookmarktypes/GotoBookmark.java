package mesfavoris.internal.bookmarktypes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.google.common.collect.ImmutableMap;

import mesfavoris.BookmarksPlugin;
import mesfavoris.StatusHelper;
import mesfavoris.bookmarktype.IBookmarkLocation;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.markers.IBookmarksMarkers;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;
import mesfavoris.topics.BookmarksEvents;

public class GotoBookmark implements IGotoBookmark {

	private final List<IGotoBookmark> gotoBookmarks;
	private final IBookmarksMarkers bookmarksMarkers;
	private final IEventBroker eventBroker;

	public GotoBookmark(List<IGotoBookmark> gotoBookmarks) {
		this.eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);
		this.gotoBookmarks = new ArrayList<IGotoBookmark>(gotoBookmarks);
		this.bookmarksMarkers = BookmarksPlugin.getBookmarksMarkers();
	}

	public GotoBookmark(IEventBroker eventBroker, List<IGotoBookmark> gotoBookmarks,
			IBookmarksMarkers bookmarksMarkers) {
		this.eventBroker = eventBroker;
		this.gotoBookmarks = new ArrayList<IGotoBookmark>(gotoBookmarks);
		this.bookmarksMarkers = bookmarksMarkers;
	}

	@Override
	public boolean gotoBookmark(IWorkbenchWindow window, Bookmark bookmark, IBookmarkLocation bookmarkLocation) {
		for (IGotoBookmark gotoBookmark : gotoBookmarks) {
			if (gotoBookmark(gotoBookmark, window, bookmark, bookmarkLocation)) {
				refreshMarker(bookmark);
				postBookmarkVisited(bookmark.getId());
				return true;
			}
		}
		return false;
	}

	private boolean gotoBookmark(IGotoBookmark gotoBookmark, IWorkbenchWindow window, Bookmark bookmark, IBookmarkLocation bookmarkLocation) {
		final boolean[] result = new boolean[] { false };
		SafeRunner.run(new ISafeRunnable() {

			public void run() throws Exception {
				result[0] = gotoBookmark.gotoBookmark(window, bookmark, bookmarkLocation);
			}

			public void handleException(Throwable exception) {
				StatusHelper.logError("Error during gotoBookmark", exception);
			}
		});
		return result[0];
	}

	protected void postBookmarkVisited(BookmarkId bookmarkId) {
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
