package mesfavoris.texteditor.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStreamReader;

import org.junit.Test;

import com.google.common.io.CharStreams;

import mesfavoris.texteditor.text.FilteredCharSequence;
import mesfavoris.texteditor.text.ICharSequenceFilter;

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

	private final ICharSequenceFilter noFilter = (charSequence, index) -> index;
	
	@Test
	public void testNoFilter() {
		assertEquals("this is a string", new FilteredCharSequence("this is a string", noFilter).toString());
		assertEquals(" this is a string ", new FilteredCharSequence(" this is a string ", noFilter).toString());
		assertEquals("    this    is  a string   ",
				new FilteredCharSequence("    this    is  a string   ", noFilter).toString());	
	}
	
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
	
}
