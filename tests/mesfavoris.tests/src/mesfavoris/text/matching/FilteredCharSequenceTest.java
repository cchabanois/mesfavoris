package mesfavoris.text.matching;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStreamReader;

import org.junit.Test;

import com.google.common.io.CharStreams;

import mesfavoris.text.matching.FilteredCharSequence.ICharSequenceFilter;

public class FilteredCharSequenceTest {

	private final ICharSequenceFilter removeWhiteCharsFilter = (charSequence, index) -> {
		while (index < charSequence.length()) {
			char charAtIndex = charSequence.charAt(index);
			if (charAtIndex != ' ' && charAtIndex != '\t' && charAtIndex != '\n') {
				return index;
			}
			index++;
		}
		return index;
	};

	@Test
	public void testFilteredCharSequenceContainsOnlyFilteredChars() {
		assertEquals("thisisastring", new FilteredCharSequence("this is a string", removeWhiteCharsFilter).toString());
		assertEquals("thisisastring", new FilteredCharSequence(" this is a string ", removeWhiteCharsFilter).toString());
		assertEquals("thisisastring",
				new FilteredCharSequence("    this    is  a string   ", removeWhiteCharsFilter).toString());
	}

	@Test
	public void testGetIndex() {
		// Given
		String source = "    this    is  a string   ";

		// When
		FilteredCharSequence target = new FilteredCharSequence(source, removeWhiteCharsFilter);

		// Then
		for (int i = 0; i < source.length(); i++) {
			if (source.charAt(i) != ' ') {
				assertEquals(source.charAt(i), target.charAt(target.getIndex(i)));
			}
		}
	}

	@Test
	public void testGetIndexForFilteredIndexes() {
		// Given
		String source = "    this    is  a string   ";

		// When
		FilteredCharSequence target = new FilteredCharSequence(source, removeWhiteCharsFilter);

		// Then
		assertEquals("thisisastring", target.toString());
		assertEquals(4, target.getIndex(9));
		assertEquals(0, target.getIndex(0));
		assertEquals(13, target.getIndex(26));
	}

	@Test
	public void testGetParentIndex() {
		// Given
		String source = "    this    is  a string   ";

		// When
		FilteredCharSequence target = new FilteredCharSequence(source, removeWhiteCharsFilter);

		// Then
		for (int i = 0; i < target.length(); i++) {
			assertEquals(target.charAt(i), source.charAt(target.getParentIndex(i)));
		}
	}

	@Test
	public void testFilterDocument() throws Exception {
		// Given
		String source = CharStreams.toString(
				new InputStreamReader(this.getClass().getResourceAsStream("AbstractDocument.java.txt"), "UTF-8"));
		
		// When
		FilteredCharSequence target = new FilteredCharSequence(source, removeWhiteCharsFilter);
		
		// Then
		assertTrue(target.length() < source.length());
	}
	
}
