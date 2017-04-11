package mesfavoris.remote;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import mesfavoris.model.BookmarkId;

public class RemoteBookmarkFolder {
	public static final String PROP_READONLY = "readonly";
	public static final String PROP_BOOKMARKS_COUNT = "bookmarksCount";
	
	private final String remoteBookmarkStoreId;
	private final BookmarkId bookmarkFolderId;
	private final Map<String, String> properties;
	
	public RemoteBookmarkFolder(String remoteBookmarkStoreId, BookmarkId bookmarkFolderId, Map<String,String> properties) {
		this.remoteBookmarkStoreId = remoteBookmarkStoreId;
		this.bookmarkFolderId = bookmarkFolderId;
		this.properties = ImmutableMap.copyOf(properties);
	}
	
	public String getRemoteBookmarkStoreId() {
		return remoteBookmarkStoreId;
	}
	
	public BookmarkId getBookmarkFolderId() {
		return bookmarkFolderId;
	}

	public Map<String, String> getProperties() {
		return properties;
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
