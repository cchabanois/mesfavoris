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
 * Immutable Map of <BookmarkId, VisitedBookmark> but with getSet returning an ordered
 * set of {@link VisitedBookmark}
 */
public class VisitedBookmarks {
	private final JImmutableSet<VisitedBookmark> set;
	private final JImmutableMap<BookmarkId, VisitedBookmark> map;

	public VisitedBookmarks() {
		set = JImmutables.sortedSet(new Comparator<VisitedBookmark>() {

			@Override
			public int compare(VisitedBookmark b1, VisitedBookmark b2) {
				int result = b2.getVisitCount() - b1.getVisitCount();
				if (result == 0) {
					return b2.getBookmarkId().toString().compareTo(b1.getBookmarkId().toString());
				} else {
					return result;
				}
			}
			
		});
		map = JImmutables.map();
	}

	public List<BookmarkId> getMostVisitedBookmarks(int count) {
		return getSet().stream().map(visitedBookmark -> visitedBookmark.getBookmarkId())
				.limit(count).collect(Collectors.toList());
	}
	
	private VisitedBookmarks(JImmutableSet<VisitedBookmark> set,
			JImmutableMap<BookmarkId, VisitedBookmark> map) {
		this.set = set;
		this.map = map;
	}

	public VisitedBookmark get(BookmarkId bookmarkId) {
		return map.get(bookmarkId);
	}

	Set<VisitedBookmark> getSet() {
		return set.getSet();
	}

	public int size() {
		return set.size();
	}

	VisitedBookmarks add(VisitedBookmark visitedBookmark) {
		JImmutableMap<BookmarkId, VisitedBookmark> newMap = map;
		JImmutableSet<VisitedBookmark> newSet = set;
		VisitedBookmark oldValue = newMap.get(visitedBookmark.getBookmarkId());
		newMap = newMap.assign(visitedBookmark.getBookmarkId(), visitedBookmark);
		if (oldValue != null) {
			newSet = newSet.delete(oldValue);
		}
		newSet = newSet.insert(visitedBookmark);
		return newVisitedBookmarks(newSet, newMap);
	}

	private VisitedBookmarks newVisitedBookmarks(JImmutableSet<VisitedBookmark> newSet, JImmutableMap<BookmarkId, VisitedBookmark> newMap) {
		if (newMap == map && newSet == set) {
			return this;
		} else {
			return new VisitedBookmarks(newSet, newMap);
		}
	}
	
	VisitedBookmarks delete(BookmarkId bookmarkId) {
		VisitedBookmark visitedBookmark = get(bookmarkId);
		if (visitedBookmark == null) {
			return this;
		}
		JImmutableMap<BookmarkId, VisitedBookmark> newMap = map;
		JImmutableSet<VisitedBookmark> newSet = set;
		newMap = newMap.delete(bookmarkId);
		newSet = newSet.delete(visitedBookmark);
		return newVisitedBookmarks(newSet, newMap);
	}

}