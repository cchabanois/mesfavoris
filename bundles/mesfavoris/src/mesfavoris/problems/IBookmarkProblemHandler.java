package mesfavoris.problems;

import mesfavoris.BookmarksException;

public interface IBookmarkProblemHandler {

	String getActionMessage(BookmarkProblem bookmarkProblem);

	void handleAction(BookmarkProblem bookmarkProblem) throws BookmarksException;

}
