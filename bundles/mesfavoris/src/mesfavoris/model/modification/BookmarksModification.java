package mesfavoris.model.modification;

import mesfavoris.model.BookmarksTree;

public abstract class BookmarksModification {
	protected final BookmarksTree sourceTree;
	protected final BookmarksTree targetTree;

	public BookmarksModification(BookmarksTree sourceTree,
			BookmarksTree targetTree) {
		this.sourceTree = sourceTree;
		this.targetTree = targetTree;
	}

	public BookmarksTree getSourceTree() {
		return sourceTree;
	}

	public BookmarksTree getTargetTree() {
		return targetTree;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sourceTree == null) ? 0 : sourceTree.hashCode());
		result = prime * result + ((targetTree == null) ? 0 : targetTree.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BookmarksModification other = (BookmarksModification) obj;
		if (sourceTree == null) {
			if (other.sourceTree != null)
				return false;
		} else if (!sourceTree.equals(other.sourceTree))
			return false;
		if (targetTree == null) {
			if (other.targetTree != null)
				return false;
		} else if (!targetTree.equals(other.targetTree))
			return false;
		return true;
	}

	
	
}