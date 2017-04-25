package mesfavoris.internal.problems.messages;

import java.util.List;
import java.util.stream.Collectors;

import mesfavoris.internal.placeholders.PathPlaceholderResolver;
import mesfavoris.problems.BookmarkProblem;
import mesfavoris.problems.IBookmarkProblemErrorMessageProvider;

public class PlaceholderUndefinedProblemMessageProvider implements IBookmarkProblemErrorMessageProvider {

	@Override
	public String getErrorMessage(BookmarkProblem bookmarkProblem) {
		List<String> placeholderNames = getPlaceholderNames(bookmarkProblem);
		if (placeholderNames.size() == 1) {
			return "Placeholder undefined : " + placeholderNames.get(0);
		} else {
			return "Placeholders undefined";
		}
	}

	private List<String> getPlaceholderNames(BookmarkProblem bookmarkProblem) {
		List<String> placeholderNames = bookmarkProblem.getProperties().values().stream()
				.map(propValue -> PathPlaceholderResolver.getPlaceholderName(propValue))
				.filter(placeholderName -> placeholderName != null).distinct().collect(Collectors.toList());
		return placeholderNames;
	}	
	
}
