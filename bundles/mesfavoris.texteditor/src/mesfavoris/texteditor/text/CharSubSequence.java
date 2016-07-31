package mesfavoris.texteditor.text;

import org.eclipse.jface.text.IRegion;

public class CharSubSequence implements CharSequence {

	private final int offset;
	private final int length;
	private final CharSequence parent;

	/**
	 * 
	 * @param parent
	 * @param start
	 *            the start index, inclusive
	 * @param end
	 *            the end index, exclusive
	 */
	public CharSubSequence(CharSequence parent, int start, int end) {
		this.parent = parent;
		this.offset = start;
		this.length = end - start;
	}

	public CharSubSequence(CharSequence parent, IRegion region) {
		this.parent = parent;
		this.offset = region.getOffset();
		this.length = region.getLength();
	}
	
	@Override
	public int length() {
		return length;
	}

	@Override
	public char charAt(int index) {
		return parent.charAt(getParentIndex(index));
	}

	public int getParentIndex(int index) {
		if (index < 0) {
			throw new IndexOutOfBoundsException("index must be larger than 0");
		}
		if (index >= length) {
			throw new IndexOutOfBoundsException("index must be smaller than length");
		}
		return offset + index;		
	}
	
	@Override
	public CharSequence subSequence(int start, int end) {
		if (end < start) {
			throw new IndexOutOfBoundsException("end cannot be smaller than start");
		}
		if (start < 0) {
			throw new IndexOutOfBoundsException("start must be larger than 0");
		}
		if (end > length) {
			throw new IndexOutOfBoundsException("end must be smaller or equal than length");
		}
		return new CharSubSequence(parent, offset + start, end - start + 1);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(length);
		sb.append(this);
		return sb.toString();
	}
}
