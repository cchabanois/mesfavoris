package mesfavoris.persistence;

import java.util.Set;

import mesfavoris.model.BookmarkId;

public interface IBookmarksDatabaseDirtyStateListener {

	public void dirtyBookmarks(Set<BookmarkId> dirtyBookmarks);
	
	
}
