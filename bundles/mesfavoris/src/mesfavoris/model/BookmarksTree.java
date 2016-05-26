package mesfavoris.model;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.javimmutable.collections.tree.JImmutableTreeMap;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import mesfavoris.internal.model.BookmarksChildrenMap;
import mesfavoris.internal.model.BookmarksMap;
import mesfavoris.internal.model.BookmarksParentsMap;

/**
 * Persistent (immutable) tree of bookmarks
 * 
 * @author cchabanois
 *
 */
public class BookmarksTree implements Iterable<Bookmark> {
	private final BookmarksMap bookmarksMap;
	private final BookmarksChildrenMap childrenMap;
	private final BookmarksParentsMap parentsMap;
	private final BookmarkId rootFolderId;

	public BookmarksTree(BookmarkFolder rootFolder) {
		this.bookmarksMap = new BookmarksMap().add(rootFolder);
		this.childrenMap = new BookmarksChildrenMap();
		this.parentsMap = new BookmarksParentsMap();
		this.rootFolderId = rootFolder.getId();
	}

	private BookmarksTree(BookmarkId rootFolderId, BookmarksMap bookmarksMap, BookmarksChildrenMap childrenMap,
			BookmarksParentsMap parentsMap) {
		this.rootFolderId = rootFolderId;
		this.bookmarksMap = bookmarksMap;
		this.childrenMap = childrenMap;
		this.parentsMap = parentsMap;
	}

	private BookmarksTree createBookmarksTree(BookmarkId rootFolderId, BookmarksMap bookmarksMap,
			BookmarksChildrenMap idToChildren, BookmarksParentsMap idToParent) {
		if (rootFolderId.equals(this.rootFolderId) && bookmarksMap == this.bookmarksMap && idToChildren == this.childrenMap
				&& idToParent == parentsMap) {
			return this;
		} else {
			return new BookmarksTree(rootFolderId, bookmarksMap, idToChildren, idToParent);
		}
	}

	public BookmarksTree setPropertyValue(BookmarkId bookmarkId, String propertyName, String propertyValue) {
		Bookmark bookmark = getBookmark(bookmarkId);
		if (bookmark == null) {
			throw new IllegalArgumentException("No bookmark with id " + bookmarkId);
		}
		JImmutableTreeMap<String, String> newProperties;
		if (propertyValue == null) {
			newProperties = bookmark.properties.delete(propertyName);
		} else {
			newProperties = bookmark.properties.assign(propertyName, propertyValue);
		}
		if (newProperties == bookmark.properties) {
			return this;
		}
		if (bookmark instanceof BookmarkFolder) {
			bookmark = new BookmarkFolder(bookmark.id, newProperties);
		} else {
			bookmark = new Bookmark(bookmark.id, newProperties);
		}
		return createBookmarksTree(rootFolderId, bookmarksMap.add(bookmark), childrenMap, parentsMap);
	}

	public BookmarksTree setProperties(BookmarkId bookmarkId, Map<String, String> properties) {
		Bookmark bookmark = getBookmark(bookmarkId);
		if (bookmark == null) {
			throw new IllegalArgumentException("No bookmark with id " + bookmarkId);
		}
		if (bookmark.properties.getMap().equals(properties)) {
			return this;
		}
		if (bookmark instanceof BookmarkFolder) {
			bookmark = new BookmarkFolder(bookmark.id, properties);
		} else {
			bookmark = new Bookmark(bookmark.id, properties);
		}
		return createBookmarksTree(rootFolderId, bookmarksMap.add(bookmark), childrenMap, parentsMap);
	}

	public BookmarksTree addBookmarks(BookmarkId parentId, List<Bookmark> bookmarks) {
		checkBookmarkFolderExist(parentId);
		checkBookmarksNotInTree(bookmarks);
		List<BookmarkId> bookmarkIds = getBookmarkIds(bookmarks);
		return createBookmarksTree(rootFolderId, bookmarksMap.add(bookmarks), childrenMap.add(parentId, bookmarkIds),
				parentsMap.setParent(bookmarkIds, parentId));
	}

	private List<BookmarkId> getBookmarkIds(List<Bookmark> bookmarks) {
		return bookmarks.stream().map(b -> b.getId()).collect(Collectors.toList());
	}

	public BookmarksTree addBookmarksBefore(BookmarkId parentId, BookmarkId existingBookmarkId, List<Bookmark> bookmarks) {
		checkBookmarkFolderExist(parentId);
		checkBookmarksNotInTree(bookmarks);
		if (existingBookmarkId != null) {
			checkBookmarkHasParent(existingBookmarkId, parentId);
		}
		List<BookmarkId> bookmarkIds = getBookmarkIds(bookmarks);
		return createBookmarksTree(rootFolderId, bookmarksMap.add(bookmarks),
				childrenMap.addBefore(parentId, bookmarkIds, existingBookmarkId),
				parentsMap.setParent(bookmarkIds, parentId));
	}

	public BookmarksTree addBookmarksAfter(BookmarkId parentId, BookmarkId existingBookmarkId, List<Bookmark> bookmarks) {
		checkBookmarkFolderExist(parentId);
		checkBookmarksNotInTree(bookmarks);
		if (existingBookmarkId != null) {
			checkBookmarkHasParent(existingBookmarkId, parentId);
		}
		List<BookmarkId> bookmarkIds = getBookmarkIds(bookmarks);
		return createBookmarksTree(rootFolderId, bookmarksMap.add(bookmarks),
				childrenMap.addAfter(parentId, bookmarkIds, existingBookmarkId),
				parentsMap.setParent(bookmarkIds, parentId));
	}

	public BookmarksTree deleteBookmark(BookmarkId bookmarkId, boolean recurse) {
		Bookmark bookmark = getBookmark(bookmarkId);
		if (bookmark == null) {
			return this;
		}
		checkNotRootFolder(bookmarkId);
		BookmarksTree newBookmarksTree = this;
		if (recurse && bookmark instanceof BookmarkFolder) {
			newBookmarksTree = deleteBookmarksUnder(bookmarkId);
			return newBookmarksTree.deleteBookmark(bookmarkId, false);
		} else {
			if (childrenMap.hasChildren(bookmarkId)) {
				throw new IllegalStateException("Cannot delete non-empty folder");
			}
			BookmarkFolder parentFolder = getParentBookmark(bookmarkId);
			return createBookmarksTree(rootFolderId, bookmarksMap.delete(bookmarkId),
					childrenMap.delete(parentFolder.getId(), Lists.newArrayList(bookmarkId)),
					parentsMap.delete(bookmarkId));
		}
	}

	private BookmarksTree deleteBookmarksUnder(BookmarkId bookmarkFolderId) {
		Set<Bookmark> subTreeBookmarks = new HashSet<Bookmark>();
		getAllBookmarksUnder(bookmarkFolderId, subTreeBookmarks);
		BookmarksMap newIdToBookmark = bookmarksMap;
		BookmarksChildrenMap newIdToChildren = childrenMap;
		BookmarksParentsMap newIdToParent = parentsMap;
		for (Bookmark bookmark : subTreeBookmarks) {
			BookmarkId bookmarkId = bookmark.getId();
			newIdToBookmark = newIdToBookmark.delete(bookmarkId);
			newIdToChildren = newIdToChildren.delete(bookmarkId);
			newIdToParent = newIdToParent.delete(bookmarkId);
		}
		newIdToChildren = newIdToChildren.delete(bookmarkFolderId);
		return createBookmarksTree(rootFolderId, newIdToBookmark, newIdToChildren, newIdToParent);
	}

	private void checkBookmarksNotInTree(Collection<Bookmark> bookmarks) {
		for (Bookmark bookmark : bookmarks) {
			if (getBookmark(bookmark.getId()) != null) {
				throw new IllegalArgumentException("Bookmark already in tree");
			}
		}
	}

	private void checkBookmarkHasParent(BookmarkId bookmarkId, BookmarkId expectedParentId) {
		BookmarkFolder parent = getParentBookmark(bookmarkId);
		BookmarkId parentId = parent == null ? null : parent.getId();
		if (!Objects.equals(expectedParentId, parentId)) {
			throw new IllegalArgumentException(
					MessageFormat.format("{0} is not the parent of {1}", expectedParentId, bookmarkId));
		}
	}

	private BookmarkFolder checkBookmarkFolderExist(BookmarkId id) {
		Bookmark bookmark = getBookmark(id);
		if (bookmark == null) {
			throw new IllegalArgumentException(MessageFormat.format("No bookmark with id {0}", id));
		}
		if (!(bookmark instanceof BookmarkFolder)) {
			throw new IllegalArgumentException(MessageFormat.format("{0} is not the id of a bookmark folder", id));
		}
		return (BookmarkFolder) bookmark;
	}

	private void checkNotRootFolder(BookmarkId bookmarkId) {
		if (rootFolderId.equals(bookmarkId)) {
			throw new IllegalArgumentException("Operation invalid on root folder");
		}
	}

	private void checkNotBookmark(BookmarkId bookmark1Id, BookmarkId bookmark2Id) {
		if (bookmark1Id.equals(bookmark2Id)) {
			throw new IllegalArgumentException("Operation invalid with this bookmark");
		}
	}

	private Bookmark checkBookmarkExist(BookmarkId bookmarkId) {
		Bookmark bookmark = getBookmark(bookmarkId);
		if (bookmark == null) {
			throw new IllegalArgumentException("No bookmark with id " + bookmarkId);
		}
		return bookmark;
	}

	public Bookmark getBookmark(BookmarkId bookmarkId) {
		return bookmarksMap.get(bookmarkId);
	}

	public BookmarkFolder getParentBookmark(BookmarkId bookmarkId) {
		checkBookmarkExist(bookmarkId);
		BookmarkId parentId = parentsMap.getParent(bookmarkId);
		if (parentId == null) {
			return null;
		} else {
			return (BookmarkFolder) bookmarksMap.get(parentId);
		}
	}

	public List<Bookmark> getChildren(BookmarkId bookmarkFolderId) {
		checkBookmarkFolderExist(bookmarkFolderId);
		return childrenMap.getChildren(bookmarkFolderId).stream().map(id -> getBookmark(id))
				.collect(Collectors.toList());
	}

	public BookmarkFolder getRootFolder() {
		return (BookmarkFolder) getBookmark(rootFolderId);
	}

	public BookmarksTree subTree(BookmarkId bookmarkFolderId) {
		BookmarkFolder bookmarkFolder = checkBookmarkFolderExist(bookmarkFolderId);
		Set<Bookmark> subTreeBookmarks = new HashSet<Bookmark>();
		getAllBookmarksUnder(bookmarkFolderId, subTreeBookmarks);
		subTreeBookmarks.add(bookmarkFolder);
		BookmarksMap newIdToBookmark = bookmarksMap;
		BookmarksChildrenMap newIdToChildren = childrenMap;
		BookmarksParentsMap newIdToParent = parentsMap;
		for (Bookmark bookmark : bookmarksMap) {
			if (!subTreeBookmarks.contains(bookmark)) {
				newIdToBookmark = newIdToBookmark.delete(bookmark.getId());
				newIdToChildren = newIdToChildren.delete(bookmark.getId());
				newIdToParent = newIdToParent.delete(bookmark.getId());
			}
		}
		newIdToParent = newIdToParent.delete(bookmarkFolder.getId());
		return createBookmarksTree(bookmarkFolder.getId(), newIdToBookmark, newIdToChildren, newIdToParent);
	}

	private void getAllBookmarksUnder(BookmarkId bookmarkFolderId, Set<Bookmark> bookmarks) {
		List<Bookmark> children = childrenMap.getChildren(bookmarkFolderId).stream().map(id -> getBookmark(id))
				.collect(Collectors.toList());
		bookmarks.addAll(children);
		for (Bookmark bookmark : children) {
			if (bookmark instanceof BookmarkFolder) {
				getAllBookmarksUnder(bookmark.getId(), bookmarks);
			}
		}
	}

	public BookmarksTree move(List<BookmarkId> bookmarkIds, BookmarkId newParentId) {
		checkBookmarkFolderExist(newParentId);
		checkBookmarksCanBeMoved(bookmarkIds, newParentId);
		BookmarksParentsMap newParentsMap = parentsMap;
		BookmarksChildrenMap newChildrenMap = childrenMap;
		for (BookmarkId bookmarkId : bookmarkIds) {
			checkBookmarkExist(bookmarkId);
			checkNotRootFolder(bookmarkId);
			BookmarkFolder oldParent = getParentBookmark(bookmarkId);
			newParentsMap = newParentsMap.delete(bookmarkId);
			newChildrenMap = newChildrenMap.delete(oldParent.getId(), Lists.newArrayList(bookmarkId));
		}
		newParentsMap = newParentsMap.setParent(bookmarkIds, newParentId);
		newChildrenMap = newChildrenMap.add(newParentId, bookmarkIds);
		return createBookmarksTree(rootFolderId, bookmarksMap, newChildrenMap, newParentsMap);
	}

	private void checkBookmarksCanBeMoved(List<BookmarkId> bookmarkIds, BookmarkId newParentId) {
		Set<BookmarkId> forbiddenIds = Sets.newHashSet(getPath(newParentId));
		for (BookmarkId bookmarkId : bookmarkIds) {
			if (forbiddenIds.contains(bookmarkId)) {
				throw new IllegalArgumentException();
			}
		}
	}

	private List<BookmarkId> getPath(BookmarkId bookmarkId) {
		if (bookmarkId.equals(rootFolderId)) {
			return Lists.newArrayList(rootFolderId);
		}
		List<BookmarkId> path = getPath(getParentBookmark(bookmarkId).getId());
		path.add(bookmarkId);
		return path;
	}

	public BookmarksTree moveAfter(List<BookmarkId> bookmarkIds, BookmarkId newParentId, BookmarkId existingBookmarkId) {
		checkBookmarkFolderExist(newParentId);
		checkBookmarksCanBeMoved(bookmarkIds, newParentId);
		if (existingBookmarkId != null) {
			checkBookmarkHasParent(existingBookmarkId, newParentId);
		}
		BookmarksParentsMap newParentsMap = parentsMap;
		BookmarksChildrenMap newChildrenMap = childrenMap;
		for (BookmarkId bookmarkId : bookmarkIds) {
			checkBookmarkExist(bookmarkId);
			checkNotBookmark(bookmarkId, existingBookmarkId);
			checkNotRootFolder(bookmarkId);
			BookmarkFolder oldParent = getParentBookmark(bookmarkId);
			newParentsMap = newParentsMap.delete(bookmarkId);
			newChildrenMap = newChildrenMap.delete(oldParent.getId(), Lists.newArrayList(bookmarkId));
		}
		newParentsMap = newParentsMap.setParent(bookmarkIds, newParentId);
		newChildrenMap = newChildrenMap.addAfter(newParentId, bookmarkIds, existingBookmarkId);
		return createBookmarksTree(rootFolderId, bookmarksMap, newChildrenMap, newParentsMap);
	}

	public BookmarksTree moveBefore(List<BookmarkId> bookmarkIds, BookmarkId newParentId, BookmarkId existingBookmarkId) {
		checkBookmarkFolderExist(newParentId);
		checkBookmarksCanBeMoved(bookmarkIds, newParentId);
		if (existingBookmarkId != null) {
			checkBookmarkHasParent(existingBookmarkId, newParentId);
		}
		BookmarksParentsMap newParentsMap = parentsMap;
		BookmarksChildrenMap newChildrenMap = childrenMap;
		for (BookmarkId bookmarkId : bookmarkIds) {
			checkBookmarkExist(bookmarkId);
			checkNotBookmark(bookmarkId, existingBookmarkId);
			checkNotRootFolder(bookmarkId);
			BookmarkFolder oldParent = getParentBookmark(bookmarkId);
			newParentsMap = newParentsMap.delete(bookmarkId);
			newChildrenMap = newChildrenMap.delete(oldParent.getId(), Lists.newArrayList(bookmarkId));
		}
		newParentsMap = newParentsMap.setParent(bookmarkIds, newParentId);
		newChildrenMap = newChildrenMap.addBefore(newParentId, bookmarkIds, existingBookmarkId);
		return createBookmarksTree(rootFolderId, bookmarksMap, newChildrenMap, newParentsMap);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb, getRootFolder(), 0);
		return sb.toString();
	}

	private void toString(StringBuilder sb, Bookmark bookmark, int level) {
		for (int i = 0; i < level; i++) {
			sb.append("  ");
		}
		sb.append(bookmark.toString());
		sb.append('\n');
		if (bookmark instanceof BookmarkFolder) {
			List<Bookmark> children = getChildren(bookmark.getId());
			for (Bookmark child : children) {
				toString(sb, child, level + 1);
			}
		}
	}

	@Override
	public Iterator<Bookmark> iterator() {
		return bookmarksMap.iterator();
	}
	
	public int size() {
		return bookmarksMap.size();
	}
	
}
