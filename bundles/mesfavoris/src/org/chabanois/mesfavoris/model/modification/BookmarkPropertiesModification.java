package org.chabanois.mesfavoris.model.modification;

import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.model.BookmarksTree;

public class BookmarkPropertiesModification extends BookmarksModification {
	private final BookmarkId bookmarkId;
	
	public BookmarkPropertiesModification(BookmarksTree sourceTree,
			BookmarksTree targetTree, BookmarkId bookmarkId) {
		super(sourceTree, targetTree);
		this.bookmarkId = bookmarkId;
	}

	public BookmarkId getBookmarkId() {
		return bookmarkId;
	}

	@Override
	public String toString() {
		return "BookmarkPropertiesModification [bookmarkId=" + bookmarkId + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((bookmarkId == null) ? 0 : bookmarkId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		BookmarkPropertiesModification other = (BookmarkPropertiesModification) obj;
		if (bookmarkId == null) {
			if (other.bookmarkId != null)
				return false;
		} else if (!bookmarkId.equals(other.bookmarkId))
			return false;
		return true;
	}
	
}
