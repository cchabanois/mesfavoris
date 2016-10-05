package mesfavoris.persistence;

import java.util.Set;

import mesfavoris.model.BookmarkId;

public interface IBookmarksDirtyStateTracker {

	/**
	 * Check if the bookmarks database is dirty (with some changes not yet saved
	 * to local file and remote stores)
	 * 
	 * @return true if dirty
	 */
	default boolean isDirty() {
		return !getDirtyBookmarks().isEmpty();
	}

	/**
	 * Get the ids of dirty bookmarks (with some changes not yet saved to local
	 * file and remote stores)
	 * 
	 * @return dirty bookmarks ids
	 */
	Set<BookmarkId> getDirtyBookmarks();

	void addListener(IBookmarksDirtyStateListener listener);

	void removeListener(IBookmarksDirtyStateListener listener);

}