package mesfavoris.internal.problems.handlers;

import java.util.List;
import java.util.stream.Collectors;

import mesfavoris.internal.placeholders.PathPlaceholderResolver;
import mesfavoris.problems.BookmarkProblem;
import mesfavoris.problems.IBookmarkProblemHandler;

public class PlaceholderUndefinedProblemHandler implements IBookmarkProblemHandler {

	@Override
	public String getErrorMessage(BookmarkProblem bookmarkProblem) {
		List<String> placeholderNames = bookmarkProblem.getProperties().values().stream().map(propValue->PathPlaceholderResolver.getPlaceholderName(propValue)).distinct().collect(Collectors.toList());
		if (placeholderNames.size() == 1) {
			return "Placeholder undefined : "+placeholderNames.get(0);
		} else {
			return "Placeholders undefined";
		}
	}

	@Override
	public String getActionMessage(BookmarkProblem bookmarkProblem) {
		return null;
	}

	@Override
	public void handleAction(BookmarkProblem bookmarkProblem) {
		
	}

}
