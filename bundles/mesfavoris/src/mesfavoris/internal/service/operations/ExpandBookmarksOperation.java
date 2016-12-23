package mesfavoris.internal.service.operations;

import static mesfavoris.PathBookmarkProperties.PROP_FILE_PATH;

import java.util.List;

import org.eclipse.core.runtime.IPath;

import mesfavoris.BookmarksException;
import mesfavoris.internal.placeholders.PathPlaceholderResolver;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.modification.IBookmarksTreeModifier;
import mesfavoris.placeholders.IPathPlaceholderResolver;
import mesfavoris.placeholders.IPathPlaceholders;

public class ExpandBookmarksOperation {
	private final IPathPlaceholderResolver pathPlaceholderResolver;
	private final BookmarkDatabase bookmarkDatabase;

	public ExpandBookmarksOperation(BookmarkDatabase bookmarkDatabase, IPathPlaceholders pathPlaceholders) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.pathPlaceholderResolver = new PathPlaceholderResolver(pathPlaceholders);
	}

	public void expand(List<BookmarkId> bookmarkIds) throws BookmarksException {
		bookmarkDatabase.modify(
				bookmarksTreeModifier -> bookmarkIds.forEach(bookmarkId -> expand(bookmarksTreeModifier, bookmarkId)));
	}

	private void expand(IBookmarksTreeModifier bookmarksTreeModifier, BookmarkId bookmarkId) {
		Bookmark bookmark = bookmarksTreeModifier.getCurrentTree().getBookmark(bookmarkId);
		String filePath = bookmark.getPropertyValue(PROP_FILE_PATH);
		if (filePath == null) {
			return;
		}
		IPath path = pathPlaceholderResolver.expand(filePath);
		if (path == null || path.toString().equals(filePath)) {
			return;
		}
		bookmarksTreeModifier.setPropertyValue(bookmarkId, PROP_FILE_PATH, path.toString());
	}
}