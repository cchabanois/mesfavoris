package mesfavoris.internal.persistence;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.SubProgressMonitor;

import mesfavoris.BookmarksException;
import mesfavoris.StatusHelper;
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
import mesfavoris.persistence.IBookmarksDatabaseDirtyStateListener;
import mesfavoris.persistence.IBookmarksDatabaseDirtyStateTracker;

/**
 * Save bookmarks from a {@link BookmarkDatabase} when bookmarks are
 * added/modified/deleted ...
 * 
 * @author cchabanois
 *
 */
public class BookmarksAutoSaver implements IBookmarksDatabaseDirtyStateTracker {
	private static final int SAVE_DELAY = 2000;
	private final BookmarkDatabase bookmarkDatabase;
	private final BackgroundBookmarksModificationsHandler backgroundBookmarksModificationsHandler;
	private final LocalBookmarksSaver localBookmarksSaver;
	private final RemoteBookmarksSaver remoteBookmarksSaver;
	private final SaveModificationsHandler saveModificationsHandler = new SaveModificationsHandler();
	private final IBookmarksListener bookmarksListener;
	private final ListenerList listenerList = new ListenerList();

	public BookmarksAutoSaver(BookmarkDatabase bookmarkDatabase, LocalBookmarksSaver localBookmarksSaver,
			RemoteBookmarksSaver remoteBookmarksSaver) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.localBookmarksSaver = localBookmarksSaver;
		this.remoteBookmarksSaver = remoteBookmarksSaver;
		this.backgroundBookmarksModificationsHandler = new BackgroundBookmarksModificationsHandler("Saving bookmarks",
				bookmarkDatabase, saveModificationsHandler, SAVE_DELAY);
		this.bookmarksListener = modifications -> {
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
	public boolean isDirty() {
		return backgroundBookmarksModificationsHandler.getQueueSize() > 0;
	}

	@Override
	public Set<BookmarkId> getDirtyBookmarks() {
		List<BookmarksModification> bookmarksModifications = backgroundBookmarksModificationsHandler
				.getUnhandledEvents();
		if (bookmarksModifications.isEmpty()) {
			return Collections.emptySet();
		}
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
		return dirtyBookmarks;
	}

	@Override
	public void addListener(IBookmarksDatabaseDirtyStateListener listener) {
		listenerList.add(listener);
	}

	@Override
	public void removeListener(IBookmarksDatabaseDirtyStateListener listener) {
		listenerList.remove(listener);
	}

	private void fireDirtyBookmarksChanged(Set<BookmarkId> dirtyBookmarks) {
		Object[] listeners = listenerList.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			final IBookmarksDatabaseDirtyStateListener listener = (IBookmarksDatabaseDirtyStateListener) listeners[i];
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
			BookmarksModification latestModification = modifications.get(modifications.size() - 1);
			BookmarksTree bookmarksTree = latestModification.getTargetTree();
			localBookmarksSaver.saveBookmarks(bookmarksTree, new SubProgressMonitor(monitor, 20));
			remoteBookmarksSaver.applyModificationsToRemoteBookmarksStores(modifications,
					new SubProgressMonitor(monitor, 80));
			fireDirtyBookmarksChanged(getDirtyBookmarks());
		}

	}

}
