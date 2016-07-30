package mesfavoris.text.matching;

import org.eclipse.core.runtime.IProgressMonitor;

import mesfavoris.text.FilteredCharSequence;
import mesfavoris.text.ICharSequenceFilter;

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
		int filteredExpectedLocation = filteredCharSequence.getIndex(expectedLocation);
		int filteredIndex = fuzzyStringMatcher.find(filteredCharSequence, filteredPattern, filteredExpectedLocation,
				monitor);
		return filteredCharSequence.getParentIndex(filteredIndex);
	}

}
