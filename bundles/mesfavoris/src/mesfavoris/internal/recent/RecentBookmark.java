package mesfavoris.internal.recent;

import java.time.Instant;

import mesfavoris.model.BookmarkId;

public class RecentBookmark {
	private final BookmarkId bookmarkId;
	private final Instant instantAdded;
	
	public RecentBookmark(BookmarkId bookmarkId, Instant instantAdded) {
		this.bookmarkId = bookmarkId;
		this.instantAdded = instantAdded;
	}

	public BookmarkId getBookmarkId() {
		return bookmarkId;
	}
	
	public Instant getInstantAdded() {
		return instantAdded;
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
		RecentBookmark other = (RecentBookmark) obj;
		if (bookmarkId == null) {
			if (other.bookmarkId != null)
				return false;
		} else if (!bookmarkId.equals(other.bookmarkId))
			return false;
		return true;
	}
	
}
