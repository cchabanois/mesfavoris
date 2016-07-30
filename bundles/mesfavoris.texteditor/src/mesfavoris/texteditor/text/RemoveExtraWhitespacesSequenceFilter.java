package mesfavoris.texteditor.text;

public class RemoveExtraWhitespacesSequenceFilter implements ICharSequenceFilter {

	@Override
	public int nextCharIndex(CharSequence charSequence, int index) {
		index = removeExtraVerticalWhitespaces(charSequence, index);
		index = removeExtraHorizontalWhitespaces(charSequence, index);
		return index;
	}

	private int removeExtraVerticalWhitespaces(CharSequence charSequence, int index) {
		int index1 = nextNonVerticalWhitespaceChar(charSequence, index);
		if (index == 0) {
			index = index1;
		} else if (index1 == charSequence.length()) {
			index = index1;
		} else {
			if (index1 != index) {
				index = index1 - 1;
			}
		}

		return index;
	}	
	
	
	private int removeExtraHorizontalWhitespaces(CharSequence charSequence, int index) {
		int index1 = nextNonHorizontalWhitespaceChar(charSequence, index);
		if (index == 0) {
			index = index1;
		} else if (index1 == charSequence.length()) {
			index = index1;
		} else {
			if (index1 != index && needHorizontalSeparation(charSequence.charAt(index-1), charSequence.charAt(index1))) {
				index = index1 - 1;
			} else {
				index = index1;
			}
		}

		return index;
	}

	private boolean needHorizontalSeparation(char char1, char char2) {
		return Character.isLetterOrDigit(char1) && Character.isLetterOrDigit((int)char2);
	}
	
	private boolean isHorizontalWhitespace(char c) {
		return c == ' ' || c == '\t';
	}

	private boolean isVerticalWhitespace(char c) {
		return c == '\n';
	}

	private int nextNonHorizontalWhitespaceChar(CharSequence charSequence, int index) {
		while (index < charSequence.length() && isHorizontalWhitespace(charSequence.charAt(index))) {
			index++;
		}
		return index;
	}

	private int nextNonVerticalWhitespaceChar(CharSequence charSequence, int index) {
		while (index < charSequence.length() && isVerticalWhitespace(charSequence.charAt(index))) {
			index++;
		}
		return index;
	}	
	
}
