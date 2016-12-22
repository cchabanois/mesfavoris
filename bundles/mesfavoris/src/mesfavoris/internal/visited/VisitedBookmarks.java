package mesfavoris.internal.visited;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.javimmutable.collections.JImmutableMap;
import org.javimmutable.collections.JImmutableSet;
import org.javimmutable.collections.util.JImmutables;

import mesfavoris.model.BookmarkId;

/**
 * Immutable Map of <BookmarkId, VisitedBookmark> but with getSet returning an
 * ordered set of {@link VisitedBookmark}
 */
public class VisitedBookmarks {
	private final JImmutableSet<VisitedBookmark> byVisitCountSet;
	private final JImmutableSet<VisitedBookmark> byLatestVisitSet;
	private final JImmutableMap<BookmarkId, VisitedBookmark> map;

	public VisitedBookmarks() {
		Comparator<VisitedBookmark> comparator = (b1, b2) -> {
			int result = b2.getVisitCount() - b1.getVisitCount();
			if (result == 0) {
				return b2.getBookmarkId().toString().compareTo(b1.getBookmarkId().toString());
			} else {
				return result;
			}
		};
		byVisitCountSet = JImmutables.sortedSet(comparator);
		comparator = (b1, b2) -> {
			int result = b2.getLatestVisit().compareTo(b1.getLatestVisit());
			if (result == 0) {
				return b2.getBookmarkId().toString().compareTo(b1.getBookmarkId().toString());
			} else {
				return result;
			}
		};
		byLatestVisitSet = JImmutables.sortedSet(comparator);
		map = JImmutables.map();
	}

	public List<BookmarkId> getMostVisitedBookmarks(int count) {
		return byVisitCountSet.getSet().stream().map(visitedBookmark -> visitedBookmark.getBookmarkId()).limit(count)
				.collect(Collectors.toList());
	}

	public List<BookmarkId> getLatestVisitedBookmarks(int count) {
		return byLatestVisitSet.getSet().stream().map(visitedBookmark -> visitedBookmark.getBookmarkId()).limit(count)
				.collect(Collectors.toList());
	}
	
	private VisitedBookmarks(JImmutableSet<VisitedBookmark> byVisitCountSet,
			JImmutableSet<VisitedBookmark> byLatestVisitSet, JImmutableMap<BookmarkId, VisitedBookmark> map) {
		this.byVisitCountSet = byVisitCountSet;
		this.byLatestVisitSet = byLatestVisitSet;
		this.map = map;
	}

	VisitedBookmark get(BookmarkId bookmarkId) {
		return map.get(bookmarkId);
	}

	Set<VisitedBookmark> getSet() {
		return byVisitCountSet.getSet();
	}

	int size() {
		return byVisitCountSet.size();
	}

	VisitedBookmarks add(VisitedBookmark visitedBookmark) {
		JImmutableMap<BookmarkId, VisitedBookmark> newMap = map;
		JImmutableSet<VisitedBookmark> newByVisitCountSet = byVisitCountSet;
		JImmutableSet<VisitedBookmark> newByLatestVisitSet = byLatestVisitSet;
		VisitedBookmark oldValue = newMap.get(visitedBookmark.getBookmarkId());
		newMap = newMap.assign(visitedBookmark.getBookmarkId(), visitedBookmark);
		if (oldValue != null) {
			newByVisitCountSet = newByVisitCountSet.delete(oldValue);
			newByLatestVisitSet = newByLatestVisitSet.delete(oldValue);
		}
		newByVisitCountSet = newByVisitCountSet.insert(visitedBookmark);
		newByLatestVisitSet = newByLatestVisitSet.insert(visitedBookmark);
		return newVisitedBookmarks(newByVisitCountSet, newByLatestVisitSet, newMap);
	}

	private VisitedBookmarks newVisitedBookmarks(JImmutableSet<VisitedBookmark> newByVisitCountSet,
			JImmutableSet<VisitedBookmark> newByLatestVisitSet, JImmutableMap<BookmarkId, VisitedBookmark> newMap) {
		if (newMap == map && newByVisitCountSet == byVisitCountSet && newByLatestVisitSet == byLatestVisitSet) {
			return this;
		} else {
			return new VisitedBookmarks(newByVisitCountSet, newByLatestVisitSet, newMap);
		}
	}

	VisitedBookmarks delete(BookmarkId bookmarkId) {
		VisitedBookmark visitedBookmark = get(bookmarkId);
		if (visitedBookmark == null) {
			return this;
		}
		JImmutableMap<BookmarkId, VisitedBookmark> newMap = map;
		JImmutableSet<VisitedBookmark> newByVisitCountSet = byVisitCountSet;
		JImmutableSet<VisitedBookmark> newByLatestVisitSet = byLatestVisitSet;
		newMap = newMap.delete(bookmarkId);
		newByVisitCountSet = newByVisitCountSet.delete(visitedBookmark);
		newByLatestVisitSet = newByLatestVisitSet.delete(visitedBookmark);
		return newVisitedBookmarks(newByVisitCountSet, newByLatestVisitSet, newMap);
	}

}