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

public class CollapseBookmarksOperation {
	private final IPathPlaceholderResolver pathPlaceholderResolver;
	private final BookmarkDatabase bookmarkDatabase;
	private final List<String> pathPropertyNames;

	public CollapseBookmarksOperation(BookmarkDatabase bookmarkDatabase, IPathPlaceholders pathPlaceholders,
			List<String> pathPropertyNames) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.pathPlaceholderResolver = new PathPlaceholderResolver(pathPlaceholders);
		this.pathPropertyNames = pathPropertyNames;
	}

	public void collapse(List<BookmarkId> bookmarkIds, String... placeholderNames) throws BookmarksException {
		bookmarkDatabase.modify(bookmarksTreeModifier -> bookmarkIds
				.forEach(bookmarkId -> collapse(bookmarksTreeModifier, bookmarkId, placeholderNames)));
	}

	private void collapse(IBookmarksTreeModifier bookmarksTreeModifier, BookmarkId bookmarkId,
			String... placeholderNames) {
		Bookmark bookmark = bookmarksTreeModifier.getCurrentTree().getBookmark(bookmarkId);
		for (String pathPropertyName : pathPropertyNames) {
			String path = bookmark.getPropertyValue(pathPropertyName);
			if (path != null) {
				IPath expandedPath = pathPlaceholderResolver.expand(path);
				if (expandedPath != null) {
					String collapsedPath = pathPlaceholderResolver.collapse(expandedPath, placeholderNames);
					bookmarksTreeModifier.setPropertyValue(bookmarkId, pathPropertyName, collapsedPath);
				}
			}
		}
	}

}
