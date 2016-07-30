package mesfavoris.texteditor.text;

import java.util.Map;
import java.util.TreeMap;

/**
 * A partial view from a CharSequence. The parent {@link CharSequence} is
 * filtered using {@link ICharSequenceFilter}
 * 
 * @author cchabanois
 *
 */
public class FilteredCharSequence implements CharSequence {
	private final CharSequence parent;
	private final TreeMap<Integer, CharSubSequence> subSequences;
	private final int length;

	public FilteredCharSequence(CharSequence parent, ICharSequenceFilter filter) {
		this.parent = parent;
		this.subSequences = getSubSequences(parent, filter);
		Map.Entry<Integer, CharSubSequence> entry = subSequences.floorEntry(Integer.MAX_VALUE);
		if (entry == null) {
			this.length = 0;
		} else {
			this.length = entry.getKey() + entry.getValue().length();
		}
	}

	private TreeMap<Integer, CharSubSequence> getSubSequences(CharSequence parent, ICharSequenceFilter filter) {
		TreeMap<Integer, CharSubSequence> subSequences = new TreeMap<>();
		int lengthSource = parent.length();
		int indexSource = 0;
		int indexTarget = 0;
		int subSequenceStartIndexSource = -1;
		int subSequenceStartIndexTarget = -1;
		while (indexSource < lengthSource) {
			int previousIndexSource = indexSource;
			indexSource = filter.nextCharIndex(parent, indexSource);
			if (indexSource < previousIndexSource) {
				throw new IllegalStateException("filter is not valid");
			}
			if (subSequenceStartIndexSource == -1) {
				subSequenceStartIndexSource = indexSource;
				subSequenceStartIndexTarget = indexTarget;
			} else if (indexSource != previousIndexSource) {
				subSequences.put(subSequenceStartIndexTarget,
						new CharSubSequence(parent, subSequenceStartIndexSource, previousIndexSource));
				subSequenceStartIndexSource = indexSource;
				subSequenceStartIndexTarget = indexTarget;
			}
			indexSource++;
			indexTarget++;
		}
		if (subSequenceStartIndexSource != lengthSource) {
			subSequences.put(subSequenceStartIndexTarget,
					new CharSubSequence(parent, subSequenceStartIndexSource, lengthSource));
		}
		return subSequences;
	}

	@Override
	public int length() {
		return length;
	}

	@Override
	public char charAt(int index) {
		checkIndexValidity(index);
		Map.Entry<Integer, CharSubSequence> entry = subSequences.floorEntry(index);
		return entry.getValue().charAt(index - entry.getKey());
	}

	public int getParentIndex(int index) {
		checkIndexValidity(index);
		Map.Entry<Integer, CharSubSequence> entry = subSequences.floorEntry(index);
		return entry.getValue().getParentIndex(index - entry.getKey());
	}

	/**
	 * 
	 * @param parentIndex
	 * @return the index 
	 */
	public int getIndex(int parentIndex) {
		if (parentIndex < 0) {
			throw new IndexOutOfBoundsException("index must be larger than 0");
		}
		if (parentIndex >= parent.length()) {
			throw new IndexOutOfBoundsException("index must be smaller than length");
		}
		// use binary search
		int low = 0;
		int high = length - 1;
		while (low <= high) {
			int mid = (low + high) >>> 1;
			int midVal = getParentIndex(mid);
			if (midVal < parentIndex)
				low = mid + 1;
			else if (midVal > parentIndex)
				high = mid - 1;
			else
				return mid;
		}
		// parent char filtered
		// we return the insertion point
		return low;
	}
	
	private void checkIndexValidity(int index) throws IndexOutOfBoundsException {
		if (index < 0) {
			throw new IndexOutOfBoundsException("index must be larger than 0");
		}
		if (index >= length) {
			throw new IndexOutOfBoundsException("index must be smaller than length");
		}
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return new CharSubSequence(this, start, end);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(length);
		sb.append(this);
		return sb.toString();
	}

}
