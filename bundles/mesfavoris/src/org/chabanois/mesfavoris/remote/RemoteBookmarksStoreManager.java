package org.chabanois.mesfavoris.remote;

import java.util.Collection;
import java.util.List;

import javax.inject.Provider;

import org.chabanois.mesfavoris.model.Bookmark;
import org.chabanois.mesfavoris.model.BookmarkFolder;
import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.model.BookmarksTree;

public class RemoteBookmarksStoreManager {
	private final Provider<List<IRemoteBookmarksStore>> remoteBookmarksStoreProvider;

	public RemoteBookmarksStoreManager(Provider<List<IRemoteBookmarksStore>> remoteBookmarksStoreProvider) {
		this.remoteBookmarksStoreProvider = remoteBookmarksStoreProvider;
	}

	public Collection<IRemoteBookmarksStore> getRemoteBookmarksStores() {
		return remoteBookmarksStoreProvider.get();
	}

	public IRemoteBookmarksStore getRemoteBookmarksStore(String id) {
		return remoteBookmarksStoreProvider.get().stream().filter(store -> id.equals(store.getDescriptor().getId()))
				.findAny().orElse(null);
	}

	public RemoteBookmarkFolder getRemoteBookmarkFolderContaining(BookmarksTree bookmarksTree, BookmarkId bookmarkId) {
		Bookmark bookmark = bookmarksTree.getBookmark(bookmarkId);
		if (bookmark == null) {
			return null;
		}
		BookmarkFolder bookmarkFolder;
		if (bookmark instanceof BookmarkFolder) {
			bookmarkFolder = (BookmarkFolder) bookmark;
		} else {
			bookmarkFolder = bookmarksTree.getParentBookmark(bookmark.getId());
		}
		return getRemoteBookmarkFolderContaining(bookmarksTree, bookmarkFolder);
	}

	private RemoteBookmarkFolder getRemoteBookmarkFolderContaining(BookmarksTree bookmarksTree,
			BookmarkFolder bookmarkFolder) {
		RemoteBookmarkFolder remoteBookmarkFolder = getRemoteBookmarkFolder(bookmarkFolder.getId());
		if (remoteBookmarkFolder != null) {
			return remoteBookmarkFolder;
		}
		BookmarkFolder parent = bookmarksTree.getParentBookmark(bookmarkFolder.getId());
		if (parent == null) {
			return null;
		} else {
			return getRemoteBookmarkFolderContaining(bookmarksTree, parent);
		}
	}

	public RemoteBookmarkFolder getRemoteBookmarkFolder(BookmarkId bookmarkFolderId) {
		for (IRemoteBookmarksStore store : getRemoteBookmarksStores()) {
			if (store.getRemoteBookmarkFolderIds().contains(bookmarkFolderId)) {
				return new RemoteBookmarkFolder(store.getDescriptor().getId(), bookmarkFolderId);
			}
		}
		return null;
	}

}
