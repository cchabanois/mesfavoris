package mesfavoris.internal.views.virtual;

import org.eclipse.core.runtime.IAdaptable;

import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;

/**
 * Link to a bookmark. We cannot have the same Bookmark in a BookmarksTree
 * 
 * @author cchabanois
 *
 */
public class BookmarkLink implements IAdaptable {
	private final BookmarkId parentId;
	private final Bookmark bookmark;

	public BookmarkLink(BookmarkId parentId, Bookmark bookmark) {
		this.parentId = parentId;
		this.bookmark = bookmark;
	}

	public BookmarkId getParentId() {
		return parentId;
	}

	public Bookmark getBookmark() {
		return bookmark;
	}

	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == Bookmark.class) {
			return getBookmark();
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bookmark == null) ? 0 : bookmark.hashCode());
		result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
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
		BookmarkLink other = (BookmarkLink) obj;
		if (bookmark == null) {
			if (other.bookmark != null)
				return false;
		} else if (!bookmark.equals(other.bookmark))
			return false;
		if (parentId == null) {
			if (other.parentId != null)
				return false;
		} else if (!parentId.equals(other.parentId))
			return false;
		return true;
	}

}
