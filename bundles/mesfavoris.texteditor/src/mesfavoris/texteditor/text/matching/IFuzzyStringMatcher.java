package mesfavoris.texteditor.text.matching;

import org.eclipse.core.runtime.IProgressMonitor;

public interface IFuzzyStringMatcher {

	/**
	 * Locate the best instance of 'pattern' in 'text' near 'expectedLocation'.
	 * Returns -1 if no match found.
	 * 
	 * @param text
	 *            The text to search.
	 * @param pattern
	 *            The pattern to search for.
	 * @param expectedLocation
	 *            The location to search around.
	 * @param monitor
	 * @return Best match index or -1.
	 */	
	public int find(CharSequence text, String pattern, int expectedLocation, IProgressMonitor monitor);
	
}