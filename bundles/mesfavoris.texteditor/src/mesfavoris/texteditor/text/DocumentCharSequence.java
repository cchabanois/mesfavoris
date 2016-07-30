package mesfavoris.texteditor.text;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * Adapts an {@link IDocument} to a {@link CharSequence}
 * 
 * @author cchabanois
 *
 */
public class DocumentCharSequence implements CharSequence {
	private final IDocument document;

	public DocumentCharSequence(IDocument document) {
		this.document = document;
	}

	@Override
	public int length() {
		return document.getLength();
	}

	@Override
	public char charAt(int index) {
		try {
			return document.getChar(index);
		} catch (BadLocationException e) {
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return new CharSubSequence(this, start, end);
	}

	@Override
	public String toString() {
		return document.get();
	}

}
