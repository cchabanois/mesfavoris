package mesfavoris.internal.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.javimmutable.collections.JImmutableMap;
import org.javimmutable.collections.util.JImmutables;

import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;

/**
 * Persistent map of all bookmarks (id -> Bookmark)
 * 
 * @author cchabanois
 *
 */
public class BookmarksMap implements Iterable<Bookmark> {
	private final JImmutableMap<BookmarkId, Bookmark> idToBookmark;

	public BookmarksMap() {
		this.idToBookmark = JImmutables.map();
	}

	private BookmarksMap(JImmutableMap<BookmarkId, Bookmark> idToBookmark) {
		this.idToBookmark = idToBookmark;
	}

	@Override
	public Iterator<Bookmark> iterator() {
		return idToBookmark.valuesCursor().iterator();

	}

	public Collection<Bookmark> values() {
		return idToBookmark.getMap().values();
	}

	public Bookmark get(BookmarkId bookmarkId) {
		return idToBookmark.get(bookmarkId);
	}

	private BookmarksMap createBookmarksMap(JImmutableMap<BookmarkId, Bookmark> idToBookmark) {
		if (idToBookmark == this.idToBookmark) {
			return this;
		} else {
			return new BookmarksMap(idToBookmark);
		}
	}

	public BookmarksMap add(Bookmark bookmark) {
		return createBookmarksMap(idToBookmark.assign(bookmark.getId(), bookmark));
	}

	public BookmarksMap add(BookmarksMap bookmarksMap) {
		JImmutableMap<BookmarkId, Bookmark> newIdToBookmark = idToBookmark;
		for (Bookmark bookmark : bookmarksMap) {
			newIdToBookmark = newIdToBookmark.assign(bookmark.getId(), bookmark);
		}
		return createBookmarksMap(newIdToBookmark);
	}

	public BookmarksMap add(List<Bookmark> bookmarksToAdd) {
		JImmutableMap<BookmarkId, Bookmark> newIdToBookmark = idToBookmark;
		for (Bookmark bookmark : bookmarksToAdd) {
			newIdToBookmark = newIdToBookmark.assign(bookmark.getId(), bookmark);
		}
		return createBookmarksMap(newIdToBookmark);
	}

	public BookmarksMap delete(BookmarkId bookmarkId) {
		return createBookmarksMap(idToBookmark.delete(bookmarkId));
	}

	public int size() {
		return idToBookmark.size();
	}
	
	@Override
	public String toString() {
		return "BookmarksMap [idToBookmark=" + idToBookmark + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idToBookmark == null) ? 0 : idToBookmark.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BookmarksMap other = (BookmarksMap) obj;
		if (idToBookmark == null) {
			if (other.idToBookmark != null)
				return false;
		} else if (!idToBookmark.equals(other.idToBookmark))
			return false;
		return true;
	}

}