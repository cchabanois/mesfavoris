package mesfavoris.texteditor.text.matching;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import mesfavoris.texteditor.text.CharSubSequence;
import mesfavoris.texteditor.text.DocumentCharSequence;
import mesfavoris.texteditor.text.LowerCaseCharSequence;
import mesfavoris.texteditor.text.RemoveExtraWhitespacesSequenceFilter;

public class DocumentFuzzySearcher {
	private final float matchThreshold;
	private final IDocument document;

	public DocumentFuzzySearcher(IDocument document) {
		this(document, 0.5f);
	}

	public DocumentFuzzySearcher(IDocument document, float matchThreshold) {
		this.document = document;
		this.matchThreshold = matchThreshold;
	}

	public int findLineNumber(String lineContent, IProgressMonitor monitor) {
		return findLineNumber(-1, lineContent, monitor);
	}

	public int findLineNumber(int expectedLineNumber, String lineContent, IProgressMonitor monitor) {
		return findLineNumber(new Region(0, document.getLength()), expectedLineNumber, lineContent, monitor);
	}

	public int findLineNumber(IRegion region, String lineContent, IProgressMonitor monitor) {
		return findLineNumber(region, -1, lineContent, monitor);
	}

	public int findLineNumber(IRegion region, int expectedLineNumber, String lineContent, IProgressMonitor monitor) {
		try {
			CharSubSequence charSubSequence = new CharSubSequence(new DocumentCharSequence(document), region);
			CharSequence lowerCharSubSequence = new LowerCaseCharSequence(charSubSequence);
			int expectedLocationInSubSequence;
			if (expectedLineNumber == -1) {
				expectedLocationInSubSequence = -1;
			} else {
				expectedLocationInSubSequence = document.getLineOffset(expectedLineNumber) - region.getOffset();
			}
			IMatchScoreComputer matchScoreComputer = getMatchScoreComputer(document, region, expectedLineNumber);
			String pattern = new LowerCaseCharSequence(lineContent).toString();

			FuzzyFilteredStringMatcher fuzzyFilteredStringMatcher = new FuzzyFilteredStringMatcher(
					new FuzzyStringMatcher(matchThreshold, matchScoreComputer),
					new RemoveExtraWhitespacesSequenceFilter());
			int matchPositionInSubSequence = fuzzyFilteredStringMatcher.find(lowerCharSubSequence, pattern,
					expectedLocationInSubSequence, monitor);
			if (matchPositionInSubSequence == -1) {
				return -1;
			}
			int matchPosition = charSubSequence.getParentIndex(matchPositionInSubSequence);
			return document.getLineOfOffset(matchPosition);
		} catch (BadLocationException e) {
			return -1;
		}
	}

	private IMatchScoreComputer getMatchScoreComputer(IDocument document, IRegion region, int expectedLineNumber)
			throws BadLocationException {
		if (expectedLineNumber == -1) {
			return new ErrorCountMatchScoreComputer();
		} else {
			int matchDistance = Math.max(document.getLineOffset(expectedLineNumber) - region.getOffset(),
					region.getOffset() + region.getLength() - document.getLineOffset(expectedLineNumber));
			return new DistanceMatchScoreComputer(matchDistance);
		}
	}

}
