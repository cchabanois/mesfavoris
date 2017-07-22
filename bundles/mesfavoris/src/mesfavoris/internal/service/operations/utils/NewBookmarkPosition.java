package mesfavoris.internal.service.operations.utils;

import java.util.Optional;

import mesfavoris.model.BookmarkId;

public class NewBookmarkPosition {
	private final BookmarkId parentBookmarkId;
	private final Optional<BookmarkId> bookmarkId;

	public NewBookmarkPosition(BookmarkId parentBookmarkId) {
		this.parentBookmarkId = parentBookmarkId;
		this.bookmarkId = Optional.empty();
	}

	public NewBookmarkPosition(BookmarkId parentBookmarkId, BookmarkId bookmarkId) {
		this.parentBookmarkId = parentBookmarkId;
		this.bookmarkId = Optional.of(bookmarkId);
	}

	public BookmarkId getParentBookmarkId() {
		return parentBookmarkId;
	}

	public Optional<BookmarkId> getBookmarkId() {
		return bookmarkId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bookmarkId == null) ? 0 : bookmarkId.hashCode());
		result = prime * result + ((parentBookmarkId == null) ? 0 : parentBookmarkId.hashCode());
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
		NewBookmarkPosition other = (NewBookmarkPosition) obj;
		if (bookmarkId == null) {
			if (other.bookmarkId != null)
				return false;
		} else if (!bookmarkId.equals(other.bookmarkId))
			return false;
		if (parentBookmarkId == null) {
			if (other.parentBookmarkId != null)
				return false;
		} else if (!parentBookmarkId.equals(other.parentBookmarkId))
			return false;
		return true;
	}

}