package mesfavoris.internal.problems.handlers;

import mesfavoris.problems.BookmarkProblem;
import mesfavoris.problems.IBookmarkProblemHandler;

public class CannotGotoProblemHandler implements IBookmarkProblemHandler {

	@Override
	public String getErrorMessage(BookmarkProblem bookmarkProblem) {
		return "Cannot goto bookmark";
	}

	@Override
	public String getActionMessage(BookmarkProblem bookmarkProblem) {
		return null;
	}

	@Override
	public void handleAction(BookmarkProblem bookmarkProblem) {

	}

}
