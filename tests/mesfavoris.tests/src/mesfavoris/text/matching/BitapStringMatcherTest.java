package mesfavoris.text.matching;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStreamReader;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.CharStreams;

public class BitapStringMatcherTest {
	private BitapStringMatcher bitap;
	private String text;

	@Before
	public void setUp() throws Exception {
		text = CharStreams.toString(
				new InputStreamReader(this.getClass().getResourceAsStream("AbstractDocument.java.txt"), "UTF-8"));

		bitap = new BitapStringMatcher(new DistanceMatchScoreComputer(10000));
	}

	@Test
	public void testFind() {
		int match = bitap.find(text, "RegisteredReplace(IDocumentListener docListener", 30, new NullProgressMonitor());
		assertThat(text.substring(match))
				.startsWith("RegisteredReplace(IDocumentListener owner, IDocumentExtension.IReplace replace) {");
	}

}
