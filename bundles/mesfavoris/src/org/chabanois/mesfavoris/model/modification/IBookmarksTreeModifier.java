package org.chabanois.mesfavoris.model.modification;

import java.util.List;
import java.util.Map;

import org.chabanois.mesfavoris.model.Bookmark;
import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.model.BookmarksTree;

public interface IBookmarksTreeModifier {

	public abstract void addBookmarks(BookmarkId parentId, List<Bookmark> bookmarks);

	public abstract void addBookmarksAfter(BookmarkId parentId, BookmarkId existingBookmarkId, List<Bookmark> bookmarks);

	public abstract void addBookmarksBefore(BookmarkId parentId, BookmarkId existingBookmarkId, List<Bookmark> bookmarks);

	public abstract void deleteBookmark(BookmarkId bookmarkId, boolean recurse);

	public abstract void setPropertyValue(BookmarkId bookmarkId, String propertyName, String propertyValue);

	public abstract void setProperties(BookmarkId bookmarkId, Map<String, String> properties);

	public abstract BookmarksTree getCurrentTree();

	public void move(List<BookmarkId> bookmarkIds, BookmarkId newParentId);

	public void moveAfter(List<BookmarkId> bookmarkIds, BookmarkId newParentId, BookmarkId existingBookmarkId);

	public void moveBefore(List<BookmarkId> bookmarkIds, BookmarkId newParentId, BookmarkId existingBookmarkId);

}