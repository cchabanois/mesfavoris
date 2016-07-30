package mesfavoris.texteditor.text;

public interface ICharSequenceFilter {

	/**
	 * return the next included char
	 * 
	 * @param index
	 * @return index if charSequence.charAt(index) should be included, next
	 *         included char index otherwise, charSequence.length if there
	 *         is no char to include anymore
	 */
	int nextCharIndex(CharSequence charSequence, int index);

}