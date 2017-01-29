package mesfavoris.topics;

public class BookmarksEvents {
	public static final String BOOKMARKS_TOPIC_BASE = "mesfavoris";
	public static final String TOPIC_BOOKMARKS = BOOKMARKS_TOPIC_BASE + "/bookmarks";
	public static final String TOPIC_BOOKMARK_VISITED = BookmarksEvents.TOPIC_BOOKMARKS + "/bookmarkVisited";
	public static final String TOPIC_VISITED_BOOKMARKS_CHANGED = BOOKMARKS_TOPIC_BASE + "/visitedBookmarks/changed";
	public static final String TOPIC_RECENT_BOOKMARKS_CHANGED = BOOKMARKS_TOPIC_BASE + "/recentBookmarks/changed";
	public static final String TOPIC_NUMBERED_BOOKMARKS_CHANGED = BOOKMARKS_TOPIC_BASE + "/numberedBookmarks/changed";
	public static final String TOPIC_BOOKMARK_PROBLEMS_CHANGED = BOOKMARKS_TOPIC_BASE + "/bookmarkProblems/changed";
}
