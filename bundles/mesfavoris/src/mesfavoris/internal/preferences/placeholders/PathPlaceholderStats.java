package mesfavoris.internal.preferences.placeholders;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Provider;

import mesfavoris.internal.placeholders.PathPlaceholderResolver;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarksTree;

public class PathPlaceholderStats {
	private Map<String, Integer> stats = new ConcurrentHashMap<>();
	private final Provider<BookmarksTree> bookmarksTreeProvider;
	private final List<String> pathPropertyNames;

	public PathPlaceholderStats(Provider<BookmarksTree> bookmarksTreeProvider, List<String> pathPropertyNames) {
		this.bookmarksTreeProvider = bookmarksTreeProvider;
		this.pathPropertyNames = pathPropertyNames;
		refresh();
	}

	public int getUsageCount(String placeholderName) {
		return stats.getOrDefault(placeholderName, 0);
	}

	public void refresh() {
		BookmarksTree bookmarksTree = bookmarksTreeProvider.get();
		stats.clear();
		for (Bookmark bookmark : bookmarksTree) {
			for (String pathPropertyName : pathPropertyNames) {
				String path = bookmark.getPropertyValue(pathPropertyName);
				if (path != null) {
					String placeholderName = PathPlaceholderResolver.getPlaceholderName(path);
					if (placeholderName != null) {
						incrementPlaceholderNameCount(placeholderName);
					}
				}
			}
		}
	}

	private void incrementPlaceholderNameCount(String placeholderName) {
		int count = stats.getOrDefault(placeholderName, 0) + 1;
		stats.put(placeholderName, count);
	}

}
