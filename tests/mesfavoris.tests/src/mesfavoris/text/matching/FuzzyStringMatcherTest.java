package mesfavoris.text.matching;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStreamReader;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.CharStreams;

public class FuzzyStringMatcherTest {
	private FuzzyStringMatcher matcher;
	private String text;

	@Before
	public void setUp() throws Exception {
		text = CharStreams.toString(
				new InputStreamReader(this.getClass().getResourceAsStream("AbstractDocument.java.txt"), "UTF-8"));

		matcher = new FuzzyStringMatcher(0.5f, new DistanceMatchScoreComputer(10000));
	}

	@Test
	public void testFind() {
		int match = matcher.find(text,
				"    while (position != null && position.offset == offset) {", 12790,
				new NullProgressMonitor());
		assertThat(text.substring(match, match+100).trim())
				.startsWith("while (p != null && p.offset == offset) {");
	}
}
