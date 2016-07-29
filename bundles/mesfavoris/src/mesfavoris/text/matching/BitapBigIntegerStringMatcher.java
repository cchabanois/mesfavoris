package mesfavoris.text.matching;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * Use the bitap algorithm to find a fuzzy match.
 * 
 * This version can use a pattern of any length but is slower than
 * {@link BitapStringMatcher}
 * 
 * This is a modified version of the bitap java implementation from
 * http://code.google.com/p/google-diff-match-patch/ as starting point (Apache
 * License, Version 2.0)
 * 
 * @author cchabanois
 *
 */
public class BitapBigIntegerStringMatcher implements IFuzzyStringMatcher {
	private final float matchThreshold;
	private final IMatchScoreComputer matchScoreComputer;

	public BitapBigIntegerStringMatcher(IMatchScoreComputer matchScoreComputer) {
		this(0.5f, matchScoreComputer);
	}

	/**
	 * 
	 * @param matchThreshold
	 *            At what point is no match declared (0.0 = perfection, 1.0 =
	 *            very loose)
	 * @param matchScoreComputer
	 */
	public BitapBigIntegerStringMatcher(float matchThreshold, IMatchScoreComputer matchScoreComputer) {
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
		monitor.beginTask("Searching pattern", pattern.length());
		// Initialise the alphabet.
		Map<Character, BigInteger> alphabet = alphabet(pattern);

		// Highest score beyond which we give up.
		double scoreThreshold = matchThreshold;
		int bestLocation = -1;

		// Initialise the bit arrays.
		BigInteger matchMask = ONE.shiftLeft(pattern.length() - 1);
		bestLocation = -1;

		int startLocation = 1;
		int finishLocation = text.length() + pattern.length();
		// Empty initialization added to appease Java compiler.
		BigInteger[] last_rd = new BigInteger[0];
		for (int passNumber = 0; passNumber < pattern.length(); passNumber++) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			// Scan for the best match; each iteration allows for one more
			// error.
			BigInteger[] rd = new BigInteger[finishLocation + 2];
			rd[finishLocation + 1] = ONE.shiftLeft(passNumber).subtract(ONE);
			for (int j = finishLocation; j >= startLocation; j--) {
				BigInteger charMatch;
				if (text.length() <= j - 1 || !alphabet.containsKey(text.charAt(j - 1))) {
					// Out of range.
					charMatch = ZERO;
				} else {
					charMatch = alphabet.get(text.charAt(j - 1));
				}
				if (passNumber == 0) {
					// First pass: exact match.
					rd[j] = rd[j + 1].shiftLeft(1).or(ONE).and(charMatch);
				} else {
					// Subsequent passes: fuzzy match.
					rd[j] = rd[j + 1].shiftLeft(1).or(ONE).and(charMatch);
					rd[j] = rd[j].or(last_rd[j + 1].or(last_rd[j]).shiftLeft(1).or(BigInteger.ONE));
					rd[j] = rd[j].or(last_rd[j + 1]);
				}
				if (!rd[j].and(matchMask).equals(ZERO)) {
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
	private Map<Character, BigInteger> alphabet(String pattern) {
		Map<Character, BigInteger> alphabet = new HashMap<Character, BigInteger>();
		char[] patternChars = pattern.toCharArray();
		for (char c : patternChars) {
			alphabet.put(c, ZERO);
		}
		int i = 0;
		for (char c : patternChars) {
			alphabet.put(c, alphabet.get(c).or(ONE.shiftLeft(pattern.length() - i - 1)));
			i++;
		}
		return alphabet;
	}

}
