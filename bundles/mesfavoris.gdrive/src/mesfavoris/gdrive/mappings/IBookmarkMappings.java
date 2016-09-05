package mesfavoris.gdrive.mappings;

import java.util.Optional;
import java.util.Set;

import mesfavoris.model.BookmarkId;

public interface IBookmarkMappings {

	public Optional<BookmarkMapping> getMapping(BookmarkId bookmarkFolderId);

	public Optional<BookmarkMapping> getMapping(String fileId);

	public Set<BookmarkMapping> getMappings();
}