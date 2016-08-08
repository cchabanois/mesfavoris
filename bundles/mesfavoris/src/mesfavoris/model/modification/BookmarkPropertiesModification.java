package mesfavoris.model.modification;

import java.util.Set;

import mesfavoris.internal.model.compare.BookmarkComparer;
import mesfavoris.internal.model.compare.BookmarkComparer.BookmarkDifferences;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;

public class BookmarkPropertiesModification extends BookmarksModification {
	private final BookmarkId bookmarkId;
	private final Set<String> addedProperties;
	private final Set<String> modifiedProperties;
	private final Set<String> deletedProperties;

	public BookmarkPropertiesModification(BookmarksTree sourceTree, BookmarksTree targetTree, BookmarkId bookmarkId) {
		super(sourceTree, targetTree);
		this.bookmarkId = bookmarkId;
		BookmarkDifferences diff = new BookmarkComparer().compare(sourceTree.getBookmark(bookmarkId),
				targetTree.getBookmark(bookmarkId));
		this.addedProperties = diff.getAddedProperties();
		this.modifiedProperties = diff.getModifiedProperties();
		this.deletedProperties = diff.getDeletedProperties();
	}

	public BookmarkId getBookmarkId() {
		return bookmarkId;
	}

	public Set<String> getAddedProperties() {
		return addedProperties;
	}

	public Set<String> getModifiedProperties() {
		return modifiedProperties;
	}

	public Set<String> getDeletedProperties() {
		return deletedProperties;
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
