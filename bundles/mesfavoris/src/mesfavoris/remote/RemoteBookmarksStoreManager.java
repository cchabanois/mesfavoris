package mesfavoris.remote;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.inject.Provider;

import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;

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

	public Optional<RemoteBookmarkFolder> getRemoteBookmarkFolderContaining(BookmarksTree bookmarksTree, BookmarkId bookmarkId) {
		Bookmark bookmark = bookmarksTree.getBookmark(bookmarkId);
		if (bookmark == null) {
			return Optional.empty();
		}
		BookmarkFolder bookmarkFolder;
		if (bookmark instanceof BookmarkFolder) {
			bookmarkFolder = (BookmarkFolder) bookmark;
		} else {
			bookmarkFolder = bookmarksTree.getParentBookmark(bookmark.getId());
		}
		return getRemoteBookmarkFolderContaining(bookmarksTree, bookmarkFolder);
	}

	private Optional<RemoteBookmarkFolder> getRemoteBookmarkFolderContaining(BookmarksTree bookmarksTree,
			BookmarkFolder bookmarkFolder) {
		Optional<RemoteBookmarkFolder> remoteBookmarkFolder = getRemoteBookmarkFolder(bookmarkFolder.getId());
		if (remoteBookmarkFolder.isPresent()) {
			return remoteBookmarkFolder;
		}
		BookmarkFolder parent = bookmarksTree.getParentBookmark(bookmarkFolder.getId());
		if (parent == null) {
			return Optional.empty();
		} else {
			return getRemoteBookmarkFolderContaining(bookmarksTree, parent);
		}
	}

	public Optional<RemoteBookmarkFolder> getRemoteBookmarkFolder(BookmarkId bookmarkFolderId) {
		for (IRemoteBookmarksStore store : getRemoteBookmarksStores()) {
			Optional<RemoteBookmarkFolder> remoteBookmarkFolder = store.getRemoteBookmarkFolder(bookmarkFolderId);
			if (remoteBookmarkFolder.isPresent()) {
				return remoteBookmarkFolder;
			}
		}
		return Optional.empty();
	}

}
