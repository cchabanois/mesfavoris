package org.chabanois.mesfavoris.internal.model.copy;

import org.chabanois.mesfavoris.model.BookmarkId;

/**
 * Provides an id when copying a bookmark
 * 
 * @author cchabanois
 *
 */
public interface IBookmarkCopyIdProvider {

	BookmarkId getBookmarkCopyId(BookmarkId sourceBookmarkId);

}
