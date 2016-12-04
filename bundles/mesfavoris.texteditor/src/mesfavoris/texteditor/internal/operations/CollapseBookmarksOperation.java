package mesfavoris.texteditor.internal.operations;

import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_FILE_PATH;

import java.util.List;

import org.eclipse.core.runtime.IPath;

import mesfavoris.BookmarksException;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.modification.IBookmarksTreeModifier;
import mesfavoris.texteditor.placeholders.IPathPlaceholders;
import mesfavoris.texteditor.placeholders.PathPlaceholderResolver;

public class CollapseBookmarksOperation {
	private final PathPlaceholderResolver pathPlaceholderResolver;
	private final BookmarkDatabase bookmarkDatabase;

	public CollapseBookmarksOperation(BookmarkDatabase bookmarkDatabase, IPathPlaceholders pathPlaceholders) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.pathPlaceholderResolver = new PathPlaceholderResolver(pathPlaceholders);
	}

	public void collapse(List<BookmarkId> bookmarkIds, String... placeholderNames) throws BookmarksException {
		bookmarkDatabase.modify(bookmarksTreeModifier -> bookmarkIds
				.forEach(bookmarkId -> collapse(bookmarksTreeModifier, bookmarkId, placeholderNames)));
	}

	private void collapse(IBookmarksTreeModifier bookmarksTreeModifier, BookmarkId bookmarkId, String... placeholderNames) {
		Bookmark bookmark = bookmarksTreeModifier.getCurrentTree().getBookmark(bookmarkId);
		String filePath = bookmark.getPropertyValue(PROP_FILE_PATH);
		if (filePath == null) {
			return;
		}
		IPath path = pathPlaceholderResolver.expand(filePath);
		if (path == null) {
			return;
		}
		String collapsedPath = pathPlaceholderResolver.collapse(path, placeholderNames);
		bookmarksTreeModifier.setPropertyValue(bookmarkId, PROP_FILE_PATH, collapsedPath);
	}
}
