package mesfavoris.internal.visited;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.osgi.service.event.EventHandler;

import mesfavoris.internal.views.virtual.BookmarkLink;
import mesfavoris.internal.views.virtual.VirtualBookmarkFolder;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.topics.BookmarksEvents;

/**
 * Virtual folder containing latest visited bookmarks
 * 
 * @author cchabanois
 *
 */
public class LatestVisitedBookmarksVirtualFolder extends VirtualBookmarkFolder {
	private final IEventBroker eventBroker;
	private final BookmarkDatabase bookmarkDatabase;
	private final VisitedBookmarksDatabase visitedBookmarksDatabase;
	private final int count;
	private final EventHandler eventHandler;

	public LatestVisitedBookmarksVirtualFolder(IEventBroker eventBroker, BookmarkDatabase bookmarkDatabase,
			VisitedBookmarksDatabase visitedBookmarksDatabase, BookmarkId parentId, int count) {
		super(parentId, "Latest visited");
		this.eventBroker = eventBroker;
		this.bookmarkDatabase = bookmarkDatabase;
		this.visitedBookmarksDatabase = visitedBookmarksDatabase;
		this.count = count;
		this.eventHandler = event -> visitedBookmarksChanged((VisitedBookmarks) event.getProperty("before"),
				(VisitedBookmarks) event.getProperty("after"));
	}

	private void visitedBookmarksChanged(VisitedBookmarks before, VisitedBookmarks after) {
		if (!before.getLatestVisitedBookmarks(count).equals(after.getLatestVisitedBookmarks(count))) {
			fireChildrenChanged();
		}
	}

	@Override
	public List<BookmarkLink> getChildren() {
		BookmarksTree bookmarksTree = bookmarkDatabase.getBookmarksTree();
		return visitedBookmarksDatabase.getVisitedBookmarks().getLatestVisitedBookmarks(count).stream()
				.map(bookmarkId -> bookmarksTree.getBookmark(bookmarkId)).filter(bookmark -> bookmark != null)
				.map(bookmark -> new BookmarkLink(bookmarkFolder.getId(), bookmark)).collect(Collectors.toList());
	}

	protected void initListening() {
		eventBroker.subscribe(BookmarksEvents.TOPIC_VISITED_BOOKMARKS_CHANGED, eventHandler);
	}

	protected void stopListening() {
		eventBroker.unsubscribe(eventHandler);
	}

}
