package mesfavoris.texteditor.internal.preferences;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Provider;

import org.chabanois.mesfavoris.model.Bookmark;
import org.chabanois.mesfavoris.model.BookmarksTree;

import mesfavoris.texteditor.TextEditorBookmarkProperties;
import mesfavoris.texteditor.placeholders.PathPlaceholderResolver;

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
			String filePath = bookmark.getPropertyValue(TextEditorBookmarkProperties.PROP_FILE_PATH);
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
