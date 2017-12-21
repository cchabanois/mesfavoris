package mesfavoris.internal.model;

import java.util.List;

import org.javimmutable.collections.JImmutableMap;
import org.javimmutable.collections.util.JImmutables;

import mesfavoris.model.BookmarkId;

/**
 * Persistent map bookmarkId -> parent id
 * 
 * @author cchabanois
 *
 */
public class BookmarksParentsMap {
	private final JImmutableMap<BookmarkId, BookmarkId> idToParent;

	public BookmarksParentsMap() {
		this.idToParent = JImmutables.map();
	}

	private BookmarksParentsMap(JImmutableMap<BookmarkId, BookmarkId> idToParent) {
		this.idToParent = idToParent;
	}

	public BookmarkId getParent(BookmarkId bookmarkId) {
		return idToParent.get(bookmarkId);
	}

	private BookmarksParentsMap createBookmarksParentsMap(JImmutableMap<BookmarkId, BookmarkId> idToParent) {
		if (idToParent == this.idToParent) {
			return this;
		}
		return new BookmarksParentsMap(idToParent);
	}

	public BookmarksParentsMap setParent(BookmarkId bookmarkId, BookmarkId parentId) {
		return createBookmarksParentsMap(idToParent.assign(bookmarkId, parentId));
	}

	public BookmarksParentsMap setParent(List<BookmarkId> bookmarksToAdd, BookmarkId parentId) {
		JImmutableMap<BookmarkId, BookmarkId> newIdToParent = idToParent;
		for (BookmarkId bookmark : bookmarksToAdd) {
			newIdToParent = newIdToParent.assign(bookmark, parentId);
		}
		return createBookmarksParentsMap(newIdToParent);
	}

	public BookmarksParentsMap delete(BookmarkId bookmarkId) {
		return createBookmarksParentsMap(idToParent.delete(bookmarkId));
	}

	@Override
	public String toString() {
		return "BookmarksParentsMap [idToParent=" + idToParent + "]";
	}

}