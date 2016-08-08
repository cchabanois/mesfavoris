package mesfavoris.texteditor.text.matching;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.InputStreamReader;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.CharStreams;

import mesfavoris.texteditor.text.RemoveExtraWhitespacesSequenceFilter;
import mesfavoris.texteditor.text.matching.DistanceMatchScoreComputer;
import mesfavoris.texteditor.text.matching.FuzzyFilteredStringMatcher;
import mesfavoris.texteditor.text.matching.FuzzyStringMatcher;

public class FuzzyFilteredStringMatcherTest {
	private FuzzyFilteredStringMatcher matcher;
	private String text;

	@Before
	public void setUp() throws Exception {
		text = CharStreams.toString(
				new InputStreamReader(this.getClass().getResourceAsStream("AbstractDocument.java.txt"), "UTF-8"));

		matcher = new FuzzyFilteredStringMatcher(new FuzzyStringMatcher(0.5f, new DistanceMatchScoreComputer(10000)),
				new RemoveExtraWhitespacesSequenceFilter());
	}

	@Test
	public void testFind() {
		// When
		int match = matcher.find(text, "    while (position != null  &&  position.offset == offset) {", 12790,
				new NullProgressMonitor());
		
		// Then
		assertThat(text.substring(match, match + 100).trim()).startsWith("while (p != null && p.offset == offset) {");
	}
	
	@Test
	public void testNotFound() {
		// When
		int match = matcher.find(text, "This won't be found", 12790,
				new NullProgressMonitor());
		
		// Then
		assertEquals(-1, match);
	}
}
