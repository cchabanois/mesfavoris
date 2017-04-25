package mesfavoris.internal.problems.messages;

import mesfavoris.problems.BookmarkProblem;
import mesfavoris.problems.IBookmarkProblemErrorMessageProvider;

public class StaticBookmarkProblemErrorMessageProvider implements IBookmarkProblemErrorMessageProvider {
	private final String message;
	
	public StaticBookmarkProblemErrorMessageProvider(String message) {
		this.message = message;
	}
	
	@Override
	public String getErrorMessage(BookmarkProblem bookmarkProblem) {
		return message;
	}

}
