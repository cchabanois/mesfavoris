package mesfavoris.internal.bookmarktypes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.google.common.collect.ImmutableMap;

import mesfavoris.BookmarksPlugin;
import mesfavoris.StatusHelper;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.internal.markers.BookmarksMarkers;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;
import mesfavoris.topics.BookmarksEvents;

public class GotoBookmark implements IGotoBookmark {

	private final List<IGotoBookmark> gotoBookmarks;
	private final BookmarksMarkers bookmarksMarkers;
	private final IEventBroker eventBroker;

	public GotoBookmark(List<IGotoBookmark> gotoBookmarks) {
		this.eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);
		this.gotoBookmarks = new ArrayList<IGotoBookmark>(gotoBookmarks);
		this.bookmarksMarkers = BookmarksPlugin.getBookmarksMarkers();
	}

	public GotoBookmark(IEventBroker eventBroker, List<IGotoBookmark> gotoBookmarks,
			BookmarksMarkers bookmarksMarkers) {
		this.eventBroker = eventBroker;
		this.gotoBookmarks = new ArrayList<IGotoBookmark>(gotoBookmarks);
		this.bookmarksMarkers = bookmarksMarkers;
	}

	@Override
	public boolean gotoBookmark(IWorkbenchWindow window, Bookmark bookmark) {
		for (IGotoBookmark gotoBookmark : gotoBookmarks) {
			if (gotoBookmark(gotoBookmark, window, bookmark)) {
				addMarkerIfMissing(bookmark);
				postBookmarkVisited(bookmark.getId());
				return true;
			}
		}
		return false;
	}

	private boolean gotoBookmark(IGotoBookmark gotoBookmark, IWorkbenchWindow window, Bookmark bookmark) {
		final boolean[] result = new boolean[] { false };
		SafeRunner.run(new ISafeRunnable() {

			public void run() throws Exception {
				result[0] = gotoBookmark.gotoBookmark(window, bookmark);
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

	private void addMarkerIfMissing(Bookmark bookmark) {
		IMarker marker = bookmarksMarkers.findMarker(bookmark.getId());
		if (marker == null) {
			bookmarksMarkers.refreshMarker(bookmark.getId(), new NullProgressMonitor());
		}
	}

}
