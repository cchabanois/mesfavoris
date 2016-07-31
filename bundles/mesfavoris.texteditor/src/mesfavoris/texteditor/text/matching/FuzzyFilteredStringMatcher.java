package mesfavoris.texteditor.text.matching;

import org.eclipse.core.runtime.IProgressMonitor;

import mesfavoris.texteditor.text.FilteredCharSequence;
import mesfavoris.texteditor.text.ICharSequenceFilter;

/**
 * A fuzzy string matcher that filters the text (removing whitespaces for ex)
 * before searching for the pattern
 * 
 * @author cchabanois
 *
 */
public class FuzzyFilteredStringMatcher implements IFuzzyStringMatcher {
	private final ICharSequenceFilter filter;
	private final IFuzzyStringMatcher fuzzyStringMatcher;

	public FuzzyFilteredStringMatcher(IFuzzyStringMatcher fuzzyStringMatcher, ICharSequenceFilter filter) {
		this.filter = filter;
		this.fuzzyStringMatcher = fuzzyStringMatcher;
	}

	@Override
	public int find(CharSequence text, String pattern, int expectedLocation, IProgressMonitor monitor) {
		FilteredCharSequence filteredCharSequence = new FilteredCharSequence(text, filter);
		String filteredPattern = new FilteredCharSequence(pattern, filter).toString();
		int filteredExpectedLocation;
		if (expectedLocation == -1) {
			filteredExpectedLocation = -1;
		} else {
			filteredExpectedLocation = filteredCharSequence.getIndex(expectedLocation);
		}
		int filteredIndex = fuzzyStringMatcher.find(filteredCharSequence, filteredPattern, filteredExpectedLocation,
				monitor);
		return filteredCharSequence.getParentIndex(filteredIndex);
	}

}
