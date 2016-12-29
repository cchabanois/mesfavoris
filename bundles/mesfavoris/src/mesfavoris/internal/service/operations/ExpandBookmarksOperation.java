package mesfavoris.internal.service.operations;

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
	private final List<String> pathPropertyNames;

	public ExpandBookmarksOperation(BookmarkDatabase bookmarkDatabase, IPathPlaceholders pathPlaceholders,
			List<String> pathPropertyNames) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.pathPlaceholderResolver = new PathPlaceholderResolver(pathPlaceholders);
		this.pathPropertyNames = pathPropertyNames;
	}

	public void expand(List<BookmarkId> bookmarkIds) throws BookmarksException {
		bookmarkDatabase.modify(
				bookmarksTreeModifier -> bookmarkIds.forEach(bookmarkId -> expand(bookmarksTreeModifier, bookmarkId)));
	}

	private void expand(IBookmarksTreeModifier bookmarksTreeModifier, BookmarkId bookmarkId) {
		Bookmark bookmark = bookmarksTreeModifier.getCurrentTree().getBookmark(bookmarkId);
		for (String pathPropertyName : pathPropertyNames) {
			String filePath = bookmark.getPropertyValue(pathPropertyName);
			if (filePath != null) {
				IPath path = pathPlaceholderResolver.expand(filePath);
				if (path != null && !path.toString().equals(filePath)) {
					bookmarksTreeModifier.setPropertyValue(bookmarkId, pathPropertyName, path.toString());
				}
			}
		}
	}
}
