package mesfavoris.internal.model;

import java.util.List;

import org.javimmutable.collections.JImmutableRandomAccessList;
import org.javimmutable.collections.util.JImmutables;

import mesfavoris.model.BookmarkId;

/**
 * Persistent list of children of a given Bookmark folder
 * 
 * @author cchabanois
 *
 */
public class BookmarkFolderChildrenList {
	private final JImmutableRandomAccessList<BookmarkId> children;

	public BookmarkFolderChildrenList(List<BookmarkId> children) {
		this.children = JImmutables.ralist(children);
	}

	private BookmarkFolderChildrenList(JImmutableRandomAccessList<BookmarkId> children) {
		this.children = children;
	}

	private BookmarkFolderChildrenList createBookmarkFolderChildrenList(
			JImmutableRandomAccessList<BookmarkId> children) {
		if (children == this.children) {
			return this;
		} else {
			return new BookmarkFolderChildrenList(children);
		}
	}

	public List<BookmarkId> getBookmarks() {
		return children.getList();
	}

	public boolean isEmpty() {
		return children.isEmpty();
	}

	public BookmarkFolderChildrenList add(List<BookmarkId> bookmarksToAdd) {
		JImmutableRandomAccessList<BookmarkId> newChildren = children;
		for (BookmarkId bookmark : bookmarksToAdd) {
			newChildren = newChildren.insert(bookmark);
		}
		return createBookmarkFolderChildrenList(newChildren);
	}

	public BookmarkFolderChildrenList addBefore(List<BookmarkId> bookmarksToAdd, BookmarkId existingBookmark) {
		JImmutableRandomAccessList<BookmarkId> newChildren = children;
		int index = newChildren.getList().indexOf(existingBookmark);
		if (index == -1) {
			index = newChildren.size();
		}
		for (BookmarkId bookmark : bookmarksToAdd) {
			newChildren = newChildren.insert(index, bookmark);
			index++;
		}
		return createBookmarkFolderChildrenList(newChildren);
	}

	public BookmarkFolderChildrenList addAfter(List<BookmarkId> bookmarksToAdd, BookmarkId existingBookmark) {
		JImmutableRandomAccessList<BookmarkId> newChildren = children;
		int index = newChildren.getList().indexOf(existingBookmark);
		if (index == -1) {
			index = 0;
		} else {
			index++;
		}
		for (BookmarkId bookmark : bookmarksToAdd) {
			newChildren = newChildren.insert(index, bookmark);
			index++;
		}
		return createBookmarkFolderChildrenList(newChildren);
	}

	public BookmarkFolderChildrenList remove(List<BookmarkId> bookmarksToRemove) {
		JImmutableRandomAccessList<BookmarkId> newChildren = children;
		for (BookmarkId bookmark : bookmarksToRemove) {
			int index = newChildren.getList().indexOf(bookmark);
			if (index != -1) {
				newChildren = newChildren.delete(index);
			}
		}
		return createBookmarkFolderChildrenList(newChildren);
	}

	@Override
	public String toString() {
		return "BookmarkFolderChildrenList [children=" + children + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((children == null) ? 0 : children.hashCode());
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
		BookmarkFolderChildrenList other = (BookmarkFolderChildrenList) obj;
		if (children == null) {
			if (other.children != null)
				return false;
		} else if (!children.equals(other.children))
			return false;
		return true;
	}

}