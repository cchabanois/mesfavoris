package mesfavoris.gdrive.mappings;

import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import mesfavoris.model.BookmarkId;

public class BookmarkMappings implements IBookmarkMappings {
	private final BiMap<BookmarkId, String> mappings = Maps.synchronizedBiMap(HashBiMap.create());

	public void add(BookmarkId bookmarkFolderId, String fileId) {
		mappings.put(bookmarkFolderId, fileId);
	}

	@Override
	public String getFileId(BookmarkId bookmarkFolderId) {
		return mappings.get(bookmarkFolderId);
	}

	@Override
	public BookmarkId getBookmarkFolderId(String fileId) {
		return mappings.inverse().get(fileId);
	}

	@Override
	public Set<BookmarkId> getBookmarkFolderIds() {
		return ImmutableSet.copyOf(mappings.keySet());
	}

	@Override
	public Set<String> getFileIds() {
		return ImmutableSet.copyOf(mappings.inverse().keySet());
	}

	public void remove(BookmarkId bookmarkFolderId) {
		mappings.remove(bookmarkFolderId);
	}

}
