package mesfavoris.internal.recent;

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

public class RecentBookmarksVirtualFolder extends VirtualBookmarkFolder {
	private final IEventBroker eventBroker;
	private final BookmarkDatabase bookmarkDatabase;
	private final RecentBookmarksDatabase recentBookmarksDatabase;
	private final int count;
	private final EventHandler eventHandler;
	
	public RecentBookmarksVirtualFolder(IEventBroker eventBroker, BookmarkDatabase bookmarkDatabase,
			RecentBookmarksDatabase recentBookmarksDatabase, BookmarkId parentId, int count) {
		super(parentId, "Recent bookmarks");
		this.eventBroker = eventBroker;
		this.bookmarkDatabase = bookmarkDatabase;
		this.recentBookmarksDatabase = recentBookmarksDatabase;
		this.count = count;
		this.eventHandler = event -> recentBookmarksChanged((RecentBookmarks) event.getProperty("before"),
				(RecentBookmarks) event.getProperty("after"));
	}

	private void recentBookmarksChanged(RecentBookmarks before, RecentBookmarks after) {
		if (!before.getRecentBookmarks(count).equals(after.getRecentBookmarks(count))) {
			fireChildrenChanged();
		}
	}
	@Override
	public List<BookmarkLink> getChildren() {
		BookmarksTree bookmarksTree = bookmarkDatabase.getBookmarksTree();
		return recentBookmarksDatabase.getRecentBookmarks().getRecentBookmarks(count).stream()
				.map(bookmarkId -> bookmarksTree.getBookmark(bookmarkId)).filter(bookmark -> bookmark != null)
				.map(bookmark -> new BookmarkLink(bookmarkFolder.getId(), bookmark)).collect(Collectors.toList());

	}

	protected void initListening() {
		eventBroker.subscribe(BookmarksEvents.TOPIC_RECENT_BOOKMARKS_CHANGED, eventHandler);
	}

	protected void stopListening() {
		eventBroker.unsubscribe(eventHandler);
	}

}
