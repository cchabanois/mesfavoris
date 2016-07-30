package mesfavoris.texteditor.text;

/**
 * Lower case view from a parent {@link CharSequence}
 * 
 * @author cchabanois
 *
 */
public class LowerCaseCharSequence implements CharSequence {
	private final CharSequence parent;

	public LowerCaseCharSequence(CharSequence parent) {
		this.parent = parent;
	}

	@Override
	public int length() {
		return parent.length();
	}

	@Override
	public char charAt(int index) {
		return Character.toLowerCase(parent.charAt(index));
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return new LowerCaseCharSequence(parent.subSequence(start, end));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(length());
		sb.append(this);
		return sb.toString();
	}

}
