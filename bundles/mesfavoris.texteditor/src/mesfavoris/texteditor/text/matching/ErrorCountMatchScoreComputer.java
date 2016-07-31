package mesfavoris.texteditor.text.matching;

/**
 * An {@link IMatchScoreComputer} that only considers the errorsCount
 * 
 * @author cchabanois
 *
 */
public class ErrorCountMatchScoreComputer implements IMatchScoreComputer {

	@Override
	public double score(int errorsCount, int matchLocation, int expectedLocation, String pattern) {
		float accuracy = (float) errorsCount / pattern.length();
		return accuracy;
	}

}
