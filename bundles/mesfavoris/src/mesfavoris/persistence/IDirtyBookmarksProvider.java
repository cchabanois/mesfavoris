package mesfavoris.persistence;

import java.util.Set;

import mesfavoris.model.BookmarkId;

public interface IDirtyBookmarksProvider {

	/**
	 * Get the ids of dirty bookmarks (with some changes not yet saved to local
	 * file and remote stores)
	 * 
	 * @return dirty bookmarks ids
	 */
	Set<BookmarkId> getDirtyBookmarks();	
	
}
