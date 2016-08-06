package mesfavoris.internal.jobs;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import mesfavoris.BookmarksException;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.IBookmarksListener;
import mesfavoris.model.modification.BookmarksModification;

/**
 * Handle bookmarks modifications in a background job.
 * 
 * 
 * It does not delegate to the given {@link IBookmarksModificationsHandler}
 * until there is no more modifications during given delay.
 * 
 * @author cchabanois
 *
 */
public class BackgroundBookmarksModificationsHandler {
	private final BookmarkDatabase bookmarkDatabase;
	private final IBookmarksModificationsHandler bookmarksModificationsHandler;
	private final Queue<BookmarksModification> eventsQueue = new ConcurrentLinkedQueue<BookmarksModification>();
	private final IBookmarksListener bookmarksListener;
	private final long scheduleDelay;
	private final AtomicInteger previousSize = new AtomicInteger(0);
	private final BookmarksModificationBatchHandlerJob job;

	public BackgroundBookmarksModificationsHandler(String jobName, BookmarkDatabase bookmarkDatabase,
			IBookmarksModificationsHandler bookmarksModificationsHandler, long delay) {
		job = new BookmarksModificationBatchHandlerJob(jobName);
		this.bookmarkDatabase = bookmarkDatabase;
		this.bookmarksModificationsHandler = bookmarksModificationsHandler;
		this.scheduleDelay = delay;
		this.bookmarksListener = modifications -> {
			eventsQueue.addAll(modifications);
			job.schedule(delay);
		};

	}

	public void init() {
		bookmarkDatabase.addListener(bookmarksListener);
	}

	public void close() {
		bookmarkDatabase.removeListener(bookmarksListener);
		try {
			do {
				job.join();
			} while (job.getState() != Job.NONE);
		} catch (InterruptedException e) {
			// ignore
		}
	}

	private class BookmarksModificationBatchHandlerJob extends Job {

		public BookmarksModificationBatchHandlerJob(String name) {
			super(name);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (previousSize.get() != eventsQueue.size()) {
				// don't handle modifications yet if there are new events
				previousSize.set(eventsQueue.size());
				schedule(scheduleDelay);
				return Status.OK_STATUS;
			}
			try {
				List<BookmarksModification> modifications = getBookmarksModifications();
				previousSize.set(0);
				if (modifications.size() == 0) {
					return Status.OK_STATUS;
				}
				bookmarksModificationsHandler.handle(modifications, monitor);
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

	public static interface IBookmarksModificationsHandler {

		public void handle(List<BookmarksModification> modifications, IProgressMonitor monitor)
				throws BookmarksException;

	}

}
