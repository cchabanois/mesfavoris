package mesfavoris.persistence;

import java.util.Set;

import mesfavoris.model.BookmarkId;

public interface IBookmarksDirtyStateListener {

	public void dirtyBookmarks(Set<BookmarkId> dirtyBookmarks);
	
	
}
