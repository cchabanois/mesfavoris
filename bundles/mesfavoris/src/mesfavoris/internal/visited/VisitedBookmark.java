package mesfavoris.internal.visited;

import mesfavoris.model.BookmarkId;

/**
 * A visited bookmark
 * 
 * @author cchabanois
 *
 */
public class VisitedBookmark {
	private final BookmarkId bookmarkId;
	private final int visitCount;

	public VisitedBookmark(BookmarkId bookmarkId, int visitCount) {
		this.bookmarkId = bookmarkId;
		this.visitCount = visitCount;
	}

	public BookmarkId getBookmarkId() {
		return bookmarkId;
	}

	public int getVisitCount() {
		return visitCount;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bookmarkId == null) ? 0 : bookmarkId.hashCode());
		result = prime * result + visitCount;
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
		VisitedBookmark other = (VisitedBookmark) obj;
		if (bookmarkId == null) {
			if (other.bookmarkId != null)
				return false;
		} else if (!bookmarkId.equals(other.bookmarkId))
			return false;
		if (visitCount != other.visitCount)
			return false;
		return true;
	}

}