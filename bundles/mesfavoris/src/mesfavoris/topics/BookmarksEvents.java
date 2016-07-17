package mesfavoris.topics;

public class BookmarksEvents {
	public static final String BOOKMARKS_TOPIC_BASE = "org/chabanois/mesfavoris";
	public static final String TOPIC_BOOKMARKS = BOOKMARKS_TOPIC_BASE + "/bookmarks";
	public static final String TOPIC_BOOKMARK_VISITED = BookmarksEvents.TOPIC_BOOKMARKS + "/bookmarkVisited";
	public static final String TOPIC_VISITED_BOOKMARKS_CHANGED = BOOKMARKS_TOPIC_BASE + "/visitedBookmarks/changed";
}
