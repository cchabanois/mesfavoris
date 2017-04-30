package mesfavoris.internal.problems.handlers;

import java.util.HashMap;
import java.util.Map;

import mesfavoris.BookmarksException;
import mesfavoris.MesFavoris;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.problems.BookmarkProblem;
import mesfavoris.problems.IBookmarkProblemHandler;
import mesfavoris.problems.IBookmarkProblems;

/**
 * Handler for {@link BookmarkProblem#TYPE_PROPERTIES_NEED_UPDATE} and
 * {@link BookmarkProblem#TYPE_PROPERTIES_MAY_UPDATE}
 * 
 * @author cchabanois
 *
 */
public class PropertiesUpdateProblemHandler implements IBookmarkProblemHandler {
	private final BookmarkDatabase bookmarkDatabase;
	private final IBookmarkProblems bookmarkProblems;

	public PropertiesUpdateProblemHandler() {
		this(MesFavoris.getBookmarkDatabase(), MesFavoris.getBookmarkProblems());
	}

	public PropertiesUpdateProblemHandler(BookmarkDatabase bookmarkDatabase, IBookmarkProblems bookmarkProblems) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.bookmarkProblems = bookmarkProblems;
	}

	@Override
	public String getActionMessage(BookmarkProblem bookmarkProblem) {
		return "Use new properties";
	}

	@Override
	public void handleAction(BookmarkProblem bookmarkProblem) throws BookmarksException {
		bookmarkDatabase.modify(bookmarksTreeModifier -> {
			BookmarkId bookmarkId = bookmarkProblem.getBookmarkId();
			Bookmark bookmark = bookmarksTreeModifier.getCurrentTree().getBookmark(bookmarkId);
			if (bookmark == null) {
				return;
			}
			Map<String, String> properties = new HashMap<>(bookmark.getProperties());
			properties.putAll(bookmarkProblem.getProperties());
			bookmarksTreeModifier.setProperties(bookmarkId, properties);
		});
		bookmarkProblems.delete(bookmarkProblem);
	}

}
