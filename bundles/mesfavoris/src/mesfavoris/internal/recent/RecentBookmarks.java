package mesfavoris.internal.recent;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.javimmutable.collections.JImmutableMap;
import org.javimmutable.collections.JImmutableSet;
import org.javimmutable.collections.util.JImmutables;

import mesfavoris.model.BookmarkId;

public class RecentBookmarks {
	private final JImmutableSet<RecentBookmark> byCreationDateSet;
	private final JImmutableMap<BookmarkId, RecentBookmark> map;
	private final Duration recentDuration;

	public RecentBookmarks(Duration recentDuration) {
		this.recentDuration = recentDuration;
		Comparator<RecentBookmark> comparator = (b1, b2) -> {
			int result = b2.getInstantAdded().compareTo(b1.getInstantAdded());
			if (result == 0) {
				return b2.getBookmarkId().toString().compareTo(b1.getBookmarkId().toString());
			} else {
				return result;
			}
		};
		byCreationDateSet = JImmutables.sortedSet(comparator);
		map = JImmutables.map();
	}

	private RecentBookmarks(JImmutableSet<RecentBookmark> byCreationDateSet,
			JImmutableMap<BookmarkId, RecentBookmark> map, Duration recentDuration) {
		this.byCreationDateSet = byCreationDateSet;
		this.map = map;
		this.recentDuration = recentDuration;
	}

	public RecentBookmarks add(RecentBookmark recentBookmark) {
		JImmutableMap<BookmarkId, RecentBookmark> newMap = map;
		JImmutableSet<RecentBookmark> newByCreationDateSet = byCreationDateSet;
		RecentBookmark oldValue = newMap.get(recentBookmark.getBookmarkId());
		newMap = newMap.assign(recentBookmark.getBookmarkId(), recentBookmark);
		if (oldValue != null) {
			newByCreationDateSet = newByCreationDateSet.delete(oldValue);
		}
		newByCreationDateSet = newByCreationDateSet.insert(recentBookmark);
		return newRecentBookmarks(newByCreationDateSet, newMap, true);
	}

	public RecentBookmarks delete(BookmarkId bookmarkId) {
		return delete(bookmarkId, true);
	}

	public RecentBookmarks delete(BookmarkId bookmarkId, boolean deleteNoMoreRecent) {
		RecentBookmark recentBookmark = get(bookmarkId);
		if (recentBookmark == null) {
			return this;
		}
		JImmutableMap<BookmarkId, RecentBookmark> newMap = map;
		JImmutableSet<RecentBookmark> newByCreationDateSet = byCreationDateSet;
		newMap = newMap.delete(bookmarkId);
		newByCreationDateSet = newByCreationDateSet.delete(recentBookmark);
		return newRecentBookmarks(newByCreationDateSet, newMap, deleteNoMoreRecent);
	}

	public RecentBookmark get(BookmarkId bookmarkId) {
		return map.get(bookmarkId);
	}

	private RecentBookmarks newRecentBookmarks(JImmutableSet<RecentBookmark> newByCreationDateSet,
			JImmutableMap<BookmarkId, RecentBookmark> newMap, boolean deleteNoMoreRecent) {
		if (newMap == map && newByCreationDateSet == byCreationDateSet) {
			return this;
		} else {
			RecentBookmarks recentBookmarks = new RecentBookmarks(newByCreationDateSet, newMap, recentDuration);
			return recentBookmarks.deleteNoMoreRecent();
		}
	}

	private RecentBookmarks deleteNoMoreRecent() {
		Instant noMoreRecentInstant = Instant.now().minus(recentDuration);
		RecentBookmarks[] recentBookmarks = { this };
		getSet().stream().filter(recentBookmark -> recentBookmark.getInstantAdded().isBefore(noMoreRecentInstant))
				.forEach(recentBookmark -> recentBookmarks[0] = delete(recentBookmark.getBookmarkId(), false));
		return recentBookmarks[0];
	}

	public List<BookmarkId> getRecentBookmarks(int count) {
		return byCreationDateSet.getSet().stream().map(recentBookmark -> recentBookmark.getBookmarkId()).limit(count)
				.collect(Collectors.toList());
	}

	public Set<RecentBookmark> getSet() {
		return byCreationDateSet.getSet();
	}

	public int size() {
		return byCreationDateSet.size();
	}

	public Duration getRecentDuration() {
		return recentDuration;
	}

}
