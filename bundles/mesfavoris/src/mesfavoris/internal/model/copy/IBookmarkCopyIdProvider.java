package mesfavoris.internal.model.copy;

import mesfavoris.model.BookmarkId;

/**
 * Provides an id when copying a bookmark
 * 
 * @author cchabanois
 *
 */
public interface IBookmarkCopyIdProvider {

	BookmarkId getBookmarkCopyId(BookmarkId sourceBookmarkId);

}
