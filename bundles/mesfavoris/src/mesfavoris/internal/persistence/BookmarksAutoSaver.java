package mesfavoris.internal.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import mesfavoris.BookmarksException;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarksTree;
import mesfavoris.model.IBookmarksListener;
import mesfavoris.model.modification.BookmarksModification;

/**
 * Save bookmarks from a {@link BookmarkDatabase} when bookmarks are added/modified/deleted ...
 * 
 * @author cchabanois
 *
 */
public class BookmarksAutoSaver {
	private static final int SAVE_DELAY = 2000;
	private final BookmarkDatabase bookmarkDatabase;
	private final IBookmarksListener bookmarksListener;
	private final SaveJob saveJob = new SaveJob();
	private final Queue<BookmarksModification> eventsQueue = new ConcurrentLinkedQueue<BookmarksModification>();
	private final LocalBookmarksSaver localBookmarksSaver;
	private final RemoteBookmarksSaver remoteBookmarksSaver;

	public BookmarksAutoSaver(BookmarkDatabase bookmarkDatabase, LocalBookmarksSaver localBookmarksSaver,
			RemoteBookmarksSaver remoteBookmarksSaver) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.localBookmarksSaver = localBookmarksSaver;
		this.remoteBookmarksSaver = remoteBookmarksSaver;
		this.bookmarksListener = modifications -> {
			eventsQueue.addAll(modifications);
			saveJob.schedule(SAVE_DELAY);
		};
	}

	public void init() {
		bookmarkDatabase.addListener(bookmarksListener);
	}

	public void close() {
		bookmarkDatabase.removeListener(bookmarksListener);
	}

	private class SaveJob extends Job {
		private int previousSize = 0;

		public SaveJob() {
			super("Saving bookmarks");
			this.setSystem(true);
		}

		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			if (previousSize != eventsQueue.size()) {
				// don't save yet if there are new events
				previousSize = eventsQueue.size();
				schedule(SAVE_DELAY);
				return Status.OK_STATUS;
			}
			try {
				List<BookmarksModification> bookmarksModifications = getBookmarksModifications();
				BookmarksModification latestModification = bookmarksModifications
						.get(bookmarksModifications.size() - 1);
				BookmarksTree bookmarksTree = latestModification.getTargetTree();
				localBookmarksSaver.saveBookmarks(bookmarksTree, new SubProgressMonitor(monitor, 20));
				remoteBookmarksSaver.applyModificationsToRemoteBookmarksStores(bookmarksModifications,
						new SubProgressMonitor(monitor, 80));
			} catch (BookmarksException e) {
				return e.getStatus();
			}
			return Status.OK_STATUS;
		}

		private List<BookmarksModification> getBookmarksModifications() {
			List<BookmarksModification> list = new ArrayList<>();
			BookmarksModification event;
			while ((event = eventsQueue.poll()) != null) {
				list.add(event);
			}
			return list;
		}

	}

}
