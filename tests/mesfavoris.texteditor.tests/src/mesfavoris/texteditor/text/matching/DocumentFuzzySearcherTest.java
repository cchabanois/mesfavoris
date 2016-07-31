package mesfavoris.texteditor.text.matching;

import static org.junit.Assert.assertEquals;

import java.io.InputStreamReader;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.CharStreams;

public class DocumentFuzzySearcherTest {
	private DocumentFuzzySearcher searcher;
	private IDocument document;

	@Before
	public void setUp() throws Exception {
		document = new Document(CharStreams.toString(
				new InputStreamReader(this.getClass().getResourceAsStream("AbstractDocument.java.txt"), "UTF-8")));
		searcher = new DocumentFuzzySearcher(document);
	}

	@Test
	public void testFindLine() {

		// When
		int lineNumber = searcher.findLineNumber(450,
				"private int computeIndexInPosition(List positions, int offset, boolean orderedByOffset) {",
				new NullProgressMonitor());

		// Then
		assertEquals(468, lineNumber);
	}

	@Test
	public void testFindLineWithoutExpectedLineNumber() {
		// When
		int lineNumber = searcher.findLineNumber(
				"private int computeIndexInPosition(List positions, int offset, boolean orderedByOffset) {",
				new NullProgressMonitor());

		// Then
		assertEquals(468, lineNumber);
	}

	@Test
	public void testFindLineInRegion() throws Exception {
		// When
		IRegion region = getRegion(document, 458, 483);

		// When
		int lineNumber = searcher.findLineNumber(region, 470,
				"private int computeIndexInPosition(List positions, int offset, boolean orderedByOffset) {",
				new NullProgressMonitor());

		// Then
		assertEquals(468, lineNumber);

	}

	private IRegion getRegion(IDocument document, int line1, int line2) throws BadLocationException {
		int offset1 = document.getLineOffset(line1);
		int offset2 = document.getLineOffset(line2);
		int length = offset2 - offset1 + 1;
		IRegion region = new Region(offset1, length);
		return region;
	}

}
