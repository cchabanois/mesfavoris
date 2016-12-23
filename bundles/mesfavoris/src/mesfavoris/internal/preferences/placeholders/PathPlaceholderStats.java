package mesfavoris.internal.preferences.placeholders;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Provider;

import mesfavoris.PathBookmarkProperties;
import mesfavoris.internal.placeholders.PathPlaceholderResolver;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarksTree;

public class PathPlaceholderStats {
	private Map<String, Integer> stats = new ConcurrentHashMap<>();
	private Provider<BookmarksTree> bookmarksTreeProvider;

	public PathPlaceholderStats(Provider<BookmarksTree> bookmarksTreeProvider) {
		this.bookmarksTreeProvider = bookmarksTreeProvider;
		refresh();
	}

	public int getUsageCount(String placeholderName) {
		return stats.getOrDefault(placeholderName, 0);
	}

	public void refresh() {
		BookmarksTree bookmarksTree = bookmarksTreeProvider.get();
		stats.clear();
		for (Bookmark bookmark : bookmarksTree) {
			String filePath = bookmark.getPropertyValue(PathBookmarkProperties.PROP_FILE_PATH);
			if (filePath != null) {
				String placeholderName = PathPlaceholderResolver.getPlaceholderName(filePath);
				if (placeholderName != null) {
					incrementPlaceholderNameCount(placeholderName);
				}
			}
		}
	}

	private void incrementPlaceholderNameCount(String placeholderName) {
		int count = stats.getOrDefault(placeholderName, 0) + 1;
		stats.put(placeholderName, count);
	}

}