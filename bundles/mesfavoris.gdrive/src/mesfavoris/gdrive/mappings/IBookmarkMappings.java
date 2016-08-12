package mesfavoris.gdrive.mappings;

import java.util.Set;

import mesfavoris.model.BookmarkId;

public interface IBookmarkMappings {

	String getFileId(BookmarkId bookmarkFolderId);

	BookmarkId getBookmarkFolderId(String fileId);

	Set<BookmarkId> getBookmarkFolderIds();

	Set<String> getFileIds();
}