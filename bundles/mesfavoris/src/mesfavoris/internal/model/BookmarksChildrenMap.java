package mesfavoris.internal.model;

import java.util.Collections;
import java.util.List;

import org.javimmutable.collections.JImmutableMap;
import org.javimmutable.collections.util.JImmutables;

import mesfavoris.model.BookmarkId;

/**
 * Peristent map bookmarkFolder -> children
 * 
 * @author cchabanois
 *
 */
public class BookmarksChildrenMap {
	private final JImmutableMap<BookmarkId, BookmarkFolderChildrenList> idToChildren;

	public BookmarksChildrenMap() {
		this.idToChildren = JImmutables.map();
	}

	private BookmarksChildrenMap(JImmutableMap<BookmarkId, BookmarkFolderChildrenList> idToChildren) {
		this.idToChildren = idToChildren;
	}

	public boolean hasChildren(BookmarkId bookmarkId) {
		return idToChildren.get(bookmarkId) != null;
	}

	private BookmarksChildrenMap createBookmarksChildrenMap(
			JImmutableMap<BookmarkId, BookmarkFolderChildrenList> idToChildren) {
		if (idToChildren == this.idToChildren) {
			return this;
		} else {
			return new BookmarksChildrenMap(idToChildren);
		}
	}

	public BookmarksChildrenMap delete(BookmarkId parentId) {
		return createBookmarksChildrenMap(idToChildren.delete(parentId));
	}

	public BookmarksChildrenMap add(BookmarksChildrenMap bookmarks) {
		JImmutableMap<BookmarkId, BookmarkFolderChildrenList> newIdToChildren = idToChildren;
		for (JImmutableMap.Entry<BookmarkId, BookmarkFolderChildrenList> entry : bookmarks.idToChildren.cursor()) {
			newIdToChildren = newIdToChildren.assign(entry.getKey(), entry.getValue());
		}
		return createBookmarksChildrenMap(newIdToChildren);
	}

	public BookmarksChildrenMap add(BookmarkId parentId, List<BookmarkId> bookmarksToAdd) {
		if (bookmarksToAdd.isEmpty()) {
			return this;
		}
		BookmarkFolderChildrenList children = idToChildren.get(parentId);
		if (children == null) {
			children = new BookmarkFolderChildrenList(bookmarksToAdd);
		} else {
			children = children.add(bookmarksToAdd);
		}
		JImmutableMap<BookmarkId, BookmarkFolderChildrenList> newIdToChildren = idToChildren.assign(parentId, children);
		return createBookmarksChildrenMap(newIdToChildren);
	}

	public BookmarksChildrenMap addBefore(BookmarkId parentId, List<BookmarkId> bookmarksToAdd, BookmarkId existingBookmark) {
		if (bookmarksToAdd.isEmpty()) {
			return this;
		}
		BookmarkFolderChildrenList children = idToChildren.get(parentId);
		if (children == null) {
			children = new BookmarkFolderChildrenList(bookmarksToAdd);
		} else {
			children = children.addBefore(bookmarksToAdd, existingBookmark);
		}
		JImmutableMap<BookmarkId, BookmarkFolderChildrenList> newIdToChildren = idToChildren.assign(parentId, children);
		return createBookmarksChildrenMap(newIdToChildren);
	}

	public BookmarksChildrenMap addAfter(BookmarkId parentId, List<BookmarkId> bookmarksToAdd, BookmarkId existingBookmark) {
		if (bookmarksToAdd.isEmpty()) {
			return this;
		}
		BookmarkFolderChildrenList newChildren = idToChildren.get(parentId);
		if (newChildren == null) {
			newChildren = new BookmarkFolderChildrenList(bookmarksToAdd);
		} else {
			newChildren = newChildren.addAfter(bookmarksToAdd, existingBookmark);
		}
		JImmutableMap<BookmarkId, BookmarkFolderChildrenList> newIdToChildren = idToChildren.assign(parentId, newChildren);
		return createBookmarksChildrenMap(newIdToChildren);
	}

	public BookmarksChildrenMap delete(BookmarkId parentId, List<BookmarkId> bookmarksToRemove) {
		if (bookmarksToRemove.isEmpty()) {
			return this;
		}
		BookmarkFolderChildrenList newChildrenList = getNewChildrenListWhenRemoving(parentId, bookmarksToRemove);
		if (newChildrenList == null) {
			return this;
		}
		if (newChildrenList.isEmpty()) {
			return createBookmarksChildrenMap(idToChildren.delete(parentId));
		} else {
			return createBookmarksChildrenMap(idToChildren.assign(parentId, newChildrenList));
		}
	}

	private BookmarkFolderChildrenList getNewChildrenListWhenRemoving(BookmarkId parentId, List<BookmarkId> bookmarksToRemove) {
		BookmarkFolderChildrenList children = idToChildren.get(parentId);
		if (children != null) {
			children = children.remove(bookmarksToRemove);
		}
		return children;
	}

	public List<BookmarkId> getChildren(BookmarkId bookmarkFolderId) {
		BookmarkFolderChildrenList children = idToChildren.get(bookmarkFolderId);
		if (children == null) {
			return Collections.emptyList();
		} else {
			return children.getBookmarks();
		}
	}

	@Override
	public String toString() {
		return "BookmarksChildrenMap [idToChildren=" + idToChildren + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idToChildren == null) ? 0 : idToChildren.hashCode());
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
		BookmarksChildrenMap other = (BookmarksChildrenMap) obj;
		if (idToChildren == null) {
			if (other.idToChildren != null)
				return false;
		} else if (!idToChildren.equals(other.idToChildren))
			return false;
		return true;
	}

}