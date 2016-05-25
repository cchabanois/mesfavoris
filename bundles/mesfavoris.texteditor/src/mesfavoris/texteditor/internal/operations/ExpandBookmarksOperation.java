package mesfavoris.texteditor.internal.operations;

import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_FILE_PATH;

import java.util.List;

import org.chabanois.mesfavoris.BookmarksException;
import org.chabanois.mesfavoris.model.Bookmark;
import org.chabanois.mesfavoris.model.BookmarkDatabase;
import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.model.modification.IBookmarksTreeModifier;
import org.chabanois.mesfavoris.validation.IBookmarkModificationValidator;
import org.eclipse.core.runtime.IPath;

import mesfavoris.texteditor.placeholders.IPathPlaceholders;
import mesfavoris.texteditor.placeholders.PathPlaceholderResolver;

public class ExpandBookmarksOperation {
	private final PathPlaceholderResolver pathPlaceholderResolver;
	private final BookmarkDatabase bookmarkDatabase;
	private final IBookmarkModificationValidator bookmarkModificationValidator;

	public ExpandBookmarksOperation(BookmarkDatabase bookmarkDatabase, IPathPlaceholders pathPlaceholders,
			IBookmarkModificationValidator bookmarkModificationValidator) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.pathPlaceholderResolver = new PathPlaceholderResolver(pathPlaceholders);
		this.bookmarkModificationValidator = bookmarkModificationValidator;
	}

	public void expand(List<BookmarkId> bookmarkIds) throws BookmarksException {
		bookmarkDatabase.modify(bookmarksTreeModifier -> bookmarkIds
				.forEach(bookmarkId -> expand(bookmarksTreeModifier, bookmarkId)));
	}

	private void expand(IBookmarksTreeModifier bookmarksTreeModifier, BookmarkId bookmarkId) {
		if (!bookmarkModificationValidator.validateModification(bookmarksTreeModifier.getCurrentTree(), bookmarkId)
				.isOK()) {
			return;
		}
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
