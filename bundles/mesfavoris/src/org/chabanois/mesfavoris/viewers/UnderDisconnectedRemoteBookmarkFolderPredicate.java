package org.chabanois.mesfavoris.viewers;

import java.util.function.Predicate;

import org.chabanois.mesfavoris.model.Bookmark;
import org.chabanois.mesfavoris.model.BookmarkDatabase;
import org.chabanois.mesfavoris.remote.IRemoteBookmarksStore;
import org.chabanois.mesfavoris.remote.IRemoteBookmarksStore.State;
import org.chabanois.mesfavoris.remote.RemoteBookmarkFolder;
import org.chabanois.mesfavoris.remote.RemoteBookmarksStoreManager;

public class UnderDisconnectedRemoteBookmarkFolderPredicate implements Predicate<Bookmark> {
	private final BookmarkDatabase bookmarkDatabase;
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;

	public UnderDisconnectedRemoteBookmarkFolderPredicate(BookmarkDatabase bookmarkDatabase, RemoteBookmarksStoreManager remoteBookmarksStoreManager) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.remoteBookmarksStoreManager = remoteBookmarksStoreManager;
	}
	
	@Override
	public boolean test(Bookmark bookmark) {
		return isUnderDisconnectedRemoteBookmarkFolder(bookmark);
	}

	private boolean isUnderDisconnectedRemoteBookmarkFolder(Bookmark bookmark) {
		RemoteBookmarkFolder remoteBookmarkFolder = remoteBookmarksStoreManager
				.getRemoteBookmarkFolderContaining(bookmarkDatabase.getBookmarksTree(), bookmark.getId());
		if (remoteBookmarkFolder == null) {
			return false;
		}
		IRemoteBookmarksStore remoteBookmarksStore = remoteBookmarksStoreManager
				.getRemoteBookmarksStore(remoteBookmarkFolder.getRemoteBookmarkStoreId());
		if (remoteBookmarksStore.getState() != State.connected) {
			return true;
		} else {
			return false;
		}
	}	
	
}
