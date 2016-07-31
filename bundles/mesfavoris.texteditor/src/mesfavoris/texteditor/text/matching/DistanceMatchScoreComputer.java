package mesfavoris.texteditor.text.matching;

/**
 * An {@link IMatchScoreComputer} that considers both the error count and the
 * distance to the expected location
 * 
 * @author cchabanois
 *
 */
public class DistanceMatchScoreComputer implements IMatchScoreComputer {
	private final int matchDistance;

	/**
	 * 
	 * @param matchDistance
	 *            How far to search for a match (0 = exact location, 1000+ =
	 *            broad match). A match this many characters away from the
	 *            expected location will add 1.0 to the score (0.0 is a perfect
	 *            match).
	 */
	public DistanceMatchScoreComputer(int matchDistance) {
		this.matchDistance = matchDistance;
	}

	@Override
	public double score(int errorsCount, int matchLocation, int expectedLocation, String pattern) {
		float accuracy = (float) errorsCount / pattern.length();
		if (expectedLocation == -1) {
			return accuracy;
		}
		int proximity = Math.abs(expectedLocation - matchLocation);
		if (matchDistance == 0) {
			// Dodge divide by zero error.
			return proximity == 0 ? accuracy : 1.0;
		}
		return accuracy + (proximity / (float) matchDistance);
	}

}
