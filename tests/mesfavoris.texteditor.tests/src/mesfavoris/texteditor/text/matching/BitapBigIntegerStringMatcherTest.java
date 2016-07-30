package mesfavoris.texteditor.text.matching;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStreamReader;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.CharStreams;

import mesfavoris.texteditor.text.matching.BitapBigIntegerStringMatcher;
import mesfavoris.texteditor.text.matching.DistanceMatchScoreComputer;

public class BitapBigIntegerStringMatcherTest {
	private BitapBigIntegerStringMatcher matcher;
	private String text;

	@Before
	public void setUp() throws Exception {
		text = CharStreams.toString(
				new InputStreamReader(this.getClass().getResourceAsStream("AbstractDocument.java.txt"), "UTF-8"));

		matcher = new BitapBigIntegerStringMatcher(new DistanceMatchScoreComputer(10000));
	}

	@Test
	public void testFind() {
		int match = matcher.find(text,
				"RegisteredReplace(IDocumentListener docListener, IDocumentExtension.IReplace replace) {", 30,
				new NullProgressMonitor());
		assertThat(text.substring(match))
				.startsWith("RegisteredReplace(IDocumentListener owner, IDocumentExtension.IReplace replace) {");
	}
}
