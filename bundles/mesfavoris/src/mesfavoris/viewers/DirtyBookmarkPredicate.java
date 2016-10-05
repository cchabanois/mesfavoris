package mesfavoris.viewers;

import java.util.function.Predicate;

import mesfavoris.model.Bookmark;
import mesfavoris.persistence.IBookmarksDirtyStateTracker;

public class DirtyBookmarkPredicate implements Predicate<Bookmark> {
	private final IBookmarksDirtyStateTracker bookmarksDirtyStateTracker;

	public DirtyBookmarkPredicate(IBookmarksDirtyStateTracker bookmarksDirtyStateTracker) {
		this.bookmarksDirtyStateTracker = bookmarksDirtyStateTracker;
	}

	@Override
	public boolean test(Bookmark bookmark) {
		return bookmarksDirtyStateTracker.getDirtyBookmarks().contains(bookmark.getId());
	}

}
