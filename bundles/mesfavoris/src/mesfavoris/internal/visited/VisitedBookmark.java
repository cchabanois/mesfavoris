package mesfavoris.internal.visited;

import java.time.Instant;

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
	private final Instant latestVisit;

	public VisitedBookmark(BookmarkId bookmarkId, int visitCount, Instant latestVisit) {
		this.bookmarkId = bookmarkId;
		this.visitCount = visitCount;
		this.latestVisit = latestVisit;
	}

	public BookmarkId getBookmarkId() {
		return bookmarkId;
	}

	public int getVisitCount() {
		return visitCount;
	}

	public Instant getLatestVisit() {
		return latestVisit;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bookmarkId == null) ? 0 : bookmarkId.hashCode());
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
		return true;
	}

}