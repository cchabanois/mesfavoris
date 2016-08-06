package mesfavoris.internal.persistence;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import mesfavoris.BookmarksException;
import mesfavoris.internal.jobs.BackgroundBookmarksModificationsHandler;
import mesfavoris.internal.jobs.BackgroundBookmarksModificationsHandler.IBookmarksModificationsHandler;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarksTree;
import mesfavoris.model.modification.BookmarksModification;

/**
 * Save bookmarks from a {@link BookmarkDatabase} when bookmarks are
 * added/modified/deleted ...
 * 
 * @author cchabanois
 *
 */
public class BookmarksAutoSaver {
	private static final int SAVE_DELAY = 2000;
	private final BackgroundBookmarksModificationsHandler backgroundBookmarksModificationsHandler;
	private final LocalBookmarksSaver localBookmarksSaver;
	private final RemoteBookmarksSaver remoteBookmarksSaver;
	private final SaveModificationsHandler saveModificationsHandler = new SaveModificationsHandler();

	public BookmarksAutoSaver(BookmarkDatabase bookmarkDatabase, LocalBookmarksSaver localBookmarksSaver,
			RemoteBookmarksSaver remoteBookmarksSaver) {
		this.localBookmarksSaver = localBookmarksSaver;
		this.remoteBookmarksSaver = remoteBookmarksSaver;
		this.backgroundBookmarksModificationsHandler = new BackgroundBookmarksModificationsHandler("Saving bookmarks",
				bookmarkDatabase, saveModificationsHandler, SAVE_DELAY);
	}

	public void init() {
		backgroundBookmarksModificationsHandler.init();
	}

	public void close() {
		backgroundBookmarksModificationsHandler.close();
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
		}

	}

}
