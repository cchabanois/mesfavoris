package org.chabanois.mesfavoris.remote;

import org.chabanois.mesfavoris.model.BookmarkId;

public class RemoteBookmarkFolder {
	private final String remoteBookmarkStoreId;
	private final BookmarkId bookmarkFolderId;
	
	public RemoteBookmarkFolder(String remoteBookmarkStoreId, BookmarkId bookmarkFolderId) {
		this.remoteBookmarkStoreId = remoteBookmarkStoreId;
		this.bookmarkFolderId = bookmarkFolderId;
	}
	
	public String getRemoteBookmarkStoreId() {
		return remoteBookmarkStoreId;
	}
	
	public BookmarkId getBookmarkFolderId() {
		return bookmarkFolderId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bookmarkFolderId == null) ? 0 : bookmarkFolderId.hashCode());
		result = prime * result + ((remoteBookmarkStoreId == null) ? 0 : remoteBookmarkStoreId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RemoteBookmarkFolder other = (RemoteBookmarkFolder) obj;
		if (bookmarkFolderId == null) {
			if (other.bookmarkFolderId != null)
				return false;
		} else if (!bookmarkFolderId.equals(other.bookmarkFolderId))
			return false;
		if (remoteBookmarkStoreId == null) {
			if (other.remoteBookmarkStoreId != null)
				return false;
		} else if (!remoteBookmarkStoreId.equals(other.remoteBookmarkStoreId))
			return false;
		return true;
	}
	
	
	
}
