package mesfavoris.viewers;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;
import mesfavoris.persistence.IBookmarksDatabaseDirtyStateListener;
import mesfavoris.persistence.IBookmarksDatabaseDirtyStateTracker;

public class DirtyBookmarkPredicate implements Predicate<Bookmark> {
	private final IBookmarksDatabaseDirtyStateTracker bookmarksDatabaseDirtyStateTracker;
	private final IBookmarksDatabaseDirtyStateListener listener;
	private final AtomicReference<Set<BookmarkId>> dirtyBookmarksRef = new AtomicReference<Set<BookmarkId>>(
			Collections.emptySet());

	public DirtyBookmarkPredicate(IBookmarksDatabaseDirtyStateTracker bookmarksDatabaseDirtyStateTracker) {
		this.bookmarksDatabaseDirtyStateTracker = bookmarksDatabaseDirtyStateTracker;
		this.listener = dirtyBookmarks -> dirtyBookmarksRef.set(dirtyBookmarks);
	}

	public void init() {
		bookmarksDatabaseDirtyStateTracker.addListener(listener);
	}

	public void dispose() {
		bookmarksDatabaseDirtyStateTracker.removeListener(listener);
	}

	@Override
	public boolean test(Bookmark bookmark) {
		return dirtyBookmarksRef.get().contains(bookmark.getId());
	}

}
