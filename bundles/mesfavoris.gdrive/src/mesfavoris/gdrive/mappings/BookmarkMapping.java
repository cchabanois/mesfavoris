package mesfavoris.gdrive.mappings;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import mesfavoris.model.BookmarkId;
import mesfavoris.remote.RemoteBookmarkFolder;

public class BookmarkMapping {
	private final BookmarkId bookmarkFolderId;
	private final String fileId;
	private final Map<String,String> properties;
	public static final String PROP_SHARING_USER = "sharingUser";
	public static final String PROP_BOOKMARKS_COUNT = RemoteBookmarkFolder.PROP_BOOKMARKS_COUNT;
	public static final String PROP_READONLY = RemoteBookmarkFolder.PROP_READONLY;
	
	public BookmarkMapping(BookmarkId bookmarkFolderId, String fileId, Map<String,String> properties) {
		this.bookmarkFolderId = bookmarkFolderId;
		this.fileId = fileId;
		this.properties = ImmutableMap.copyOf(properties);
	}

	public BookmarkId getBookmarkFolderId() {
		return bookmarkFolderId;
	}
	
	public String getFileId() {
		return fileId;
	}
	
	public Map<String, String> getProperties() {
		return properties;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bookmarkFolderId == null) ? 0 : bookmarkFolderId.hashCode());
		result = prime * result + ((fileId == null) ? 0 : fileId.hashCode());
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
		BookmarkMapping other = (BookmarkMapping) obj;
		if (bookmarkFolderId == null) {
			if (other.bookmarkFolderId != null)
				return false;
		} else if (!bookmarkFolderId.equals(other.bookmarkFolderId))
			return false;
		if (fileId == null) {
			if (other.fileId != null)
				return false;
		} else if (!fileId.equals(other.fileId))
			return false;
		return true;
	}
	
}