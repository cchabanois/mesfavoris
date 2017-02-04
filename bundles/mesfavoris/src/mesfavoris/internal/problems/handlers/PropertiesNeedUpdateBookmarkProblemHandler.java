package mesfavoris.internal.problems.handlers;

import mesfavoris.problems.BookmarkProblem;
import mesfavoris.problems.IBookmarkProblemHandler;

public class PropertiesNeedUpdateBookmarkProblemHandler implements IBookmarkProblemHandler {

	@Override
	public String getErrorMessage(BookmarkProblem bookmarkProblem) {
		return "Some properties need to be updated";
	}

	@Override
	public String getActionMessage(BookmarkProblem bookmarkProblem) {
		return null;
	}

	@Override
	public void handleAction(BookmarkProblem bookmarkProblem) {

	}

}
