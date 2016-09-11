package mesfavoris.persistence;

import java.util.Set;

import mesfavoris.model.BookmarkId;

public interface IBookmarksDatabaseDirtyStateTracker {

	/**
	 * Check if the bookmarks database is dirty (with some changes not yet saved
	 * to local file and remote stores)
	 * 
	 * @return true if dirty
	 */
	boolean isDirty();

	/**
	 * Get the ids of dirty bookmarks
	 * 
	 * @return dirty bookmarks ids
	 */
	public Set<BookmarkId> getDirtyBookmarks();

	public void addListener(IBookmarksDatabaseDirtyStateListener listener);
	
	public void removeListener(IBookmarksDatabaseDirtyStateListener listener);
	
}