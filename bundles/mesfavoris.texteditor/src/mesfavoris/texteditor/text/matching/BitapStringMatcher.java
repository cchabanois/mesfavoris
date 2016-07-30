package mesfavoris.texteditor.text.matching;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * Use the bitap algorithm to find a fuzzy match.
 * 
 * This is a modified version of the bitap java implementation from
 * http://code.google.com/p/google-diff-match-patch/ as starting point (Apache
 * License, Version 2.0)
 * 
 * @author cchabanois
 *
 */
public class BitapStringMatcher implements IFuzzyStringMatcher {

	// The number of bits in a long.
	private final short maxBits = 64;

	private final float matchThreshold;
	private final IMatchScoreComputer matchScoreComputer;

	public BitapStringMatcher(IMatchScoreComputer matchScoreComputer) {
		this(0.5f, matchScoreComputer);
	}

	/**
	 * 
	 * @param matchThreshold
	 *            At what point is no match declared (0.0 = perfection, 1.0 =
	 *            very loose)
	 * @param matchScoreComputer
	 */
	public BitapStringMatcher(float matchThreshold, IMatchScoreComputer matchScoreComputer) {
		this.matchThreshold = matchThreshold;
		this.matchScoreComputer = matchScoreComputer;
	}

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
	 * @return Best match index or -1.
	 */
	public int find(CharSequence text, String pattern, int expectedLocation, IProgressMonitor monitor) {
		if (pattern.length() > maxBits) {
			throw new IllegalArgumentException("Pattern too long.");
		}

		monitor.beginTask("Searching pattern", pattern.length());
		// Initialise the alphabet.
		Map<Character, Long> alphabet = alphabet(pattern);

		// Highest score beyond which we give up.
		double scoreThreshold = matchThreshold;
		int bestLocation = -1;

		// Initialise the bit arrays.
		long matchMask = 1L << (pattern.length() - 1);
		bestLocation = -1;

		int startLocation = 1;
		int finishLocation = text.length() + pattern.length();
		// Empty initialization added to appease Java compiler.
		long[] last_rd = new long[0];
		for (int passNumber = 0; passNumber < pattern.length(); passNumber++) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			// Scan for the best match; each iteration allows for one more
			// error.
			long[] rd = new long[finishLocation + 2];
			rd[finishLocation + 1] = (1 << passNumber) - 1;
			for (int j = finishLocation; j >= startLocation; j--) {
				long charMatch;
				if (text.length() <= j - 1 || !alphabet.containsKey(text.charAt(j - 1))) {
					// Out of range.
					charMatch = 0;
				} else {
					charMatch = alphabet.get(text.charAt(j - 1));
				}
				if (passNumber == 0) {
					// First pass: exact match.
					rd[j] = ((rd[j + 1] << 1) | 1L) & charMatch;
				} else {
					// Subsequent passes: fuzzy match.
					rd[j] = (((rd[j + 1] << 1) | 1L) & charMatch) | (((last_rd[j + 1] | last_rd[j]) << 1) | 1L)
							| last_rd[j + 1];
				}
				if ((rd[j] & matchMask) != 0) {
					double score = matchScoreComputer.score(passNumber, j - 1, expectedLocation, pattern);
					// This match will almost certainly be better than any
					// existing match. But check anyway.
					if (score <= scoreThreshold) {
						// Told you so.
						scoreThreshold = score;
						bestLocation = j - 1;
					}
				}
			}
			if (matchScoreComputer.score(passNumber + 1, expectedLocation, expectedLocation,
					pattern) > scoreThreshold) {
				// No hope for a (better) match at greater error levels.
				break;
			}
			last_rd = rd;
			monitor.worked(1);
		}
		monitor.done();
		return bestLocation;
	}

	/**
	 * Initialise the alphabet for the Bitap algorithm.
	 * 
	 * @param pattern
	 *            The text to encode.
	 * @return Hash of character locations.
	 */
	private Map<Character, Long> alphabet(String pattern) {
		Map<Character, Long> alphabet = new HashMap<Character, Long>();
		char[] patternChars = pattern.toCharArray();
		for (char c : patternChars) {
			alphabet.put(c, 0L);
		}
		int i = 0;
		for (char c : patternChars) {
			alphabet.put(c, alphabet.get(c) | (1L << (pattern.length() - i - 1)));
			i++;
		}
		return alphabet;
	}

}
