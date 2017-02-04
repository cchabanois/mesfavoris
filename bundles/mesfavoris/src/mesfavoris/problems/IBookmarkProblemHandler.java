package mesfavoris.problems;

public interface IBookmarkProblemHandler {

	String getErrorMessage(BookmarkProblem bookmarkProblem);
	
	String getActionMessage(BookmarkProblem bookmarkProblem);
	
	void handleAction(BookmarkProblem bookmarkProblem);
	
}
