package mesfavoris.texteditor.text.matching;

public interface IMatchScoreComputer {

	/**
	 * Compute and return the score for a match with e errors and x location.
	 * 
	 * @param errorsCount
	 *            Number of errors in match.
	 * @param matchLocation
	 *            Location of match.
	 * @param expectedLocation
	 *            Expected location of match, -1 if unknown.
	 * @param pattern
	 *            Pattern being sought.
	 * @return Overall score for match (0.0 = good, 1.0 = bad).
	 */
	public double score(int errorsCount, int matchLocation, int expectedLocation, String pattern);
	
}
