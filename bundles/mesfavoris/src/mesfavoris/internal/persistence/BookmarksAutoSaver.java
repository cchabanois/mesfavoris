package mesfavoris.internal.persistence;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.SubMonitor;

import com.google.common.collect.ImmutableSet;

import mesfavoris.BookmarksException;
import mesfavoris.internal.StatusHelper;
import mesfavoris.internal.jobs.BackgroundBookmarksModificationsHandler;
import mesfavoris.internal.jobs.BackgroundBookmarksModificationsHandler.IBookmarksModificationsHandler;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.model.IBookmarksListener;
import mesfavoris.model.modification.BookmarkDeletedModification;
import mesfavoris.model.modification.BookmarkPropertiesModification;
import mesfavoris.model.modification.BookmarksAddedModification;
import mesfavoris.model.modification.BookmarksModification;
import mesfavoris.model.modification.BookmarksMovedModification;
import mesfavoris.persistence.IBookmarksDirtyStateListener;
import mesfavoris.persistence.IBookmarksDirtyStateTracker;

/**
 * Save bookmarks from a {@link BookmarkDatabase} when bookmarks are
 * added/modified/deleted ...
 * 
 * @author cchabanois
 *
 */
public class BookmarksAutoSaver implements IBookmarksDirtyStateTracker {
	private static final int SAVE_DELAY = 2000;
	private final BookmarkDatabase bookmarkDatabase;
	private final BackgroundBookmarksModificationsHandler backgroundBookmarksModificationsHandler;
	private final LocalBookmarksSaver localBookmarksSaver;
	private final RemoteBookmarksSaver remoteBookmarksSaver;
	private final SaveModificationsHandler saveModificationsHandler = new SaveModificationsHandler();
	private final IBookmarksListener bookmarksListener;
	private final ListenerList<IBookmarksDirtyStateListener> listenerList = new ListenerList<>();
	private final AtomicReference<Set<BookmarkId>> dirtyBookmarksRef = new AtomicReference<Set<BookmarkId>>(
			Collections.emptySet());

	public BookmarksAutoSaver(BookmarkDatabase bookmarkDatabase, LocalBookmarksSaver localBookmarksSaver,
			RemoteBookmarksSaver remoteBookmarksSaver) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.localBookmarksSaver = localBookmarksSaver;
		this.remoteBookmarksSaver = remoteBookmarksSaver;
		this.backgroundBookmarksModificationsHandler = new BackgroundBookmarksModificationsHandler("Saving bookmarks",
				bookmarkDatabase, saveModificationsHandler, SAVE_DELAY);
		this.bookmarksListener = modifications -> {
			computeDirtyBookmarks();
			fireDirtyBookmarksChanged(getDirtyBookmarks());
		};
	}

	public void init() {
		backgroundBookmarksModificationsHandler.init();
		bookmarkDatabase.addListener(bookmarksListener);
	}

	public void close() {
		bookmarkDatabase.removeListener(bookmarksListener);
		backgroundBookmarksModificationsHandler.close();
	}

	@Override
	public Set<BookmarkId> getDirtyBookmarks() {
		return dirtyBookmarksRef.get();
	}

	private void computeDirtyBookmarks() {
		List<BookmarksModification> bookmarksModifications = backgroundBookmarksModificationsHandler
				.getUnhandledEvents();
		Set<BookmarkId> dirtyBookmarks = new HashSet<>();
		for (BookmarksModification bookmarksModification : bookmarksModifications) {
			if (bookmarksModification instanceof BookmarkDeletedModification) {
				BookmarkDeletedModification bookmarkDeletedModification = (BookmarkDeletedModification) bookmarksModification;
				dirtyBookmarks.add(bookmarkDeletedModification.getBookmarkParentId());
			} else if (bookmarksModification instanceof BookmarkPropertiesModification) {
				BookmarkPropertiesModification bookmarkPropertiesModification = (BookmarkPropertiesModification) bookmarksModification;
				dirtyBookmarks.add(bookmarkPropertiesModification.getBookmarkId());
			} else if (bookmarksModification instanceof BookmarksAddedModification) {
				BookmarksAddedModification bookmarksAddedModification = (BookmarksAddedModification) bookmarksModification;
				dirtyBookmarks.add(bookmarksAddedModification.getParentId());
				dirtyBookmarks.addAll(bookmarksAddedModification.getBookmarks().stream()
						.map(bookmark -> bookmark.getId()).collect(Collectors.toList()));
			} else if (bookmarksModification instanceof BookmarksMovedModification) {
				BookmarksMovedModification bookmarksMovedModification = (BookmarksMovedModification) bookmarksModification;
				dirtyBookmarks.add(bookmarksMovedModification.getNewParentId());
			}
		}
		dirtyBookmarksRef.set(ImmutableSet.copyOf(dirtyBookmarks));
	}

	@Override
	public void addListener(IBookmarksDirtyStateListener listener) {
		listenerList.add(listener);
	}

	@Override
	public void removeListener(IBookmarksDirtyStateListener listener) {
		listenerList.remove(listener);
	}

	private void fireDirtyBookmarksChanged(Set<BookmarkId> dirtyBookmarks) {
		for (IBookmarksDirtyStateListener listener : listenerList) {
			SafeRunner.run(new ISafeRunnable() {

				public void run() throws Exception {
					listener.dirtyBookmarks(dirtyBookmarks);
				}

				public void handleException(Throwable exception) {
					StatusHelper.logError("Error in bookmarks dirty state listener", exception);
				}
			});
		}
	}

	private class SaveModificationsHandler implements IBookmarksModificationsHandler {

		@Override
		public void handle(List<BookmarksModification> modifications, IProgressMonitor monitor)
				throws BookmarksException {
			SubMonitor subMonitor = SubMonitor.convert(monitor);
			try {
				BookmarksModification latestModification = modifications.get(modifications.size() - 1);
				BookmarksTree bookmarksTree = latestModification.getTargetTree();
				localBookmarksSaver.saveBookmarks(bookmarksTree, subMonitor.split(20));
				remoteBookmarksSaver.applyModificationsToRemoteBookmarksStores(modifications, subMonitor.split(80));
			} finally {
				computeDirtyBookmarks();
				fireDirtyBookmarksChanged(getDirtyBookmarks());
			}
		}

	}

}
