package mesfavoris.internal.numberedbookmarks;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.osgi.service.event.EventHandler;

import mesfavoris.internal.views.virtual.BookmarkLink;
import mesfavoris.internal.views.virtual.VirtualBookmarkFolder;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.topics.BookmarksEvents;

public class NumberedBookmarksVirtualFolder extends VirtualBookmarkFolder {
	private final BookmarkDatabase bookmarkDatabase;
	private final NumberedBookmarks numberedBookmarks;
	private final IEventBroker eventBroker;
	private final EventHandler eventHandler;
	
	public NumberedBookmarksVirtualFolder(IEventBroker eventBroker, BookmarkDatabase bookmarkDatabase, BookmarkId parentId,
			NumberedBookmarks numberedBookmarks) {
		super(parentId, "Numbered bookmarks");
		this.bookmarkDatabase = bookmarkDatabase;
		this.numberedBookmarks = numberedBookmarks;
		this.eventBroker = eventBroker;
		this.eventHandler = event -> fireChildrenChanged();
	}

	@Override
	public List<BookmarkLink> getChildren() {
		BookmarksTree bookmarksTree = bookmarkDatabase.getBookmarksTree();
		return Arrays.stream(BookmarkNumber.values())
				.map(bookmarkNumber -> numberedBookmarks.getBookmark(bookmarkNumber)).filter(Optional::isPresent)
				.map(Optional::get).map(bookmarkId -> bookmarksTree.getBookmark(bookmarkId))
				.filter(bookmark -> bookmark != null)
				.map(bookmark -> new BookmarkLink(bookmarkFolder.getId(), bookmark)).collect(Collectors.toList());
	}

	@Override
	protected void initListening() {
		eventBroker.subscribe(BookmarksEvents.TOPIC_NUMBERED_BOOKMARKS_CHANGED, eventHandler);

	}

	@Override
	protected void stopListening() {
		eventBroker.unsubscribe(eventHandler);

	}

}
