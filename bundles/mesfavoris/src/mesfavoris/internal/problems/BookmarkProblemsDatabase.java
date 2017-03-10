package mesfavoris.internal.problems;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.services.events.IEventBroker;

import com.google.common.collect.ImmutableMap;

import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.StatusHelper;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.IBookmarksListener;
import mesfavoris.model.modification.BookmarkDeletedModification;
import mesfavoris.model.modification.BookmarksModification;
import mesfavoris.problems.BookmarkProblem;
import mesfavoris.problems.IBookmarkProblems;
import mesfavoris.topics.BookmarksEvents;

public class BookmarkProblemsDatabase implements IBookmarkProblems {
	private final AtomicReference<BookmarkProblems> bookmarkProblemsReference = new AtomicReference<BookmarkProblems>(
			new BookmarkProblems());
	private final BookmarkDatabase bookmarkDatabase;
	private final File bookmarkProblemsFile;
	private final SaveJob saveJob = new SaveJob();
	private final IEventBroker eventBroker;
	private final IBookmarksListener bookmarksListener;

	public BookmarkProblemsDatabase(IEventBroker eventBroker, BookmarkDatabase bookmarkDatabase,
			File bookmarkProblemsFile) {
		this.eventBroker = eventBroker;
		this.bookmarkDatabase = bookmarkDatabase;
		this.bookmarkProblemsFile = bookmarkProblemsFile;
		this.bookmarksListener = modifications -> bookmarksDeleted(
				filterBookmarksDeleteModifications(modifications.stream()).flatMap(
						modification -> StreamSupport.stream(modification.getDeletedBookmarks().spliterator(), false))
						.map(bookmark -> bookmark.getId()).collect(Collectors.toList()));
	}

	private Stream<BookmarkDeletedModification> filterBookmarksDeleteModifications(
			Stream<BookmarksModification> stream) {
		return stream.filter(modification -> modification instanceof BookmarkDeletedModification)
				.map(modification -> (BookmarkDeletedModification) modification);
	}

	public void init() {
		try {
			BookmarkProblemsPersister persister = new BookmarkProblemsPersister(bookmarkProblemsFile);
			bookmarkProblemsReference.set(persister.load());
		} catch (Exception e) {
			StatusHelper.logError("Could not load bookmark problems", e);
		}
		bookmarkDatabase.addListener(bookmarksListener);
	}

	public void close() throws InterruptedException {
		bookmarkDatabase.removeListener(bookmarksListener);
		do {
			saveJob.join();
		} while (saveJob.getState() != Job.NONE);
	}

	private BookmarkProblems getBookmarkProblems() {
		return bookmarkProblemsReference.get();
	}

	@Override
	public Optional<BookmarkProblem> getBookmarkProblem(BookmarkId bookmarkId, String problemType) {
		return getBookmarkProblems().getBookmarkProblem(bookmarkId, problemType);
	}

	@Override
	public int size() {
		return getBookmarkProblems().size();
	}
	
	@Override
	public Set<BookmarkProblem> getBookmarkProblems(BookmarkId bookmarkId) {
		return getBookmarkProblems().getBookmarkProblems(bookmarkId);
	}

	private void modify(Function<BookmarkProblems,BookmarkProblems> function) {
		BookmarkProblems bookmarkProblems;
		BookmarkProblems newBookmarkProblems;
		do {
			bookmarkProblems = bookmarkProblemsReference.get();
			newBookmarkProblems = bookmarkProblems;
			newBookmarkProblems = function.apply(bookmarkProblems);
			if (bookmarkProblems == newBookmarkProblems) {
				return;
			}
		} while (!bookmarkProblemsReference.compareAndSet(bookmarkProblems, newBookmarkProblems));
		postBookmarkProblemsChanged(bookmarkProblems, newBookmarkProblems);
		saveJob.schedule();
	}
	
	@Override
	public void delete(BookmarkProblem problem) {
		modify(bookmarkProblems->bookmarkProblems.delete(problem));
	}

	@Override
	public void delete(BookmarkId bookmarkId) {
		modify(bookmarkProblems->bookmarkProblems.delete(bookmarkId));
	}
	
	@Override
	public void add(BookmarkProblem problem) {
		modify(bookmarkProblems->bookmarkProblems.add(problem));
	}

	private void bookmarksDeleted(List<BookmarkId> bookmarkIds) {
		modify(bookmarkProblems->{
			for (BookmarkId bookmarkId : bookmarkIds) {
				bookmarkProblems = bookmarkProblems.delete(bookmarkId);
			}
			return bookmarkProblems;
		});
	}

	private void postBookmarkProblemsChanged(BookmarkProblems bookmarkProblems, BookmarkProblems newBookmarksProblems) {
		eventBroker.post(BookmarksEvents.TOPIC_BOOKMARK_PROBLEMS_CHANGED,
				ImmutableMap.of("before", bookmarkProblems, "after", newBookmarksProblems));
	}


	@Override
	public Iterator<BookmarkProblem> iterator() {
		return bookmarkProblemsReference.get().iterator();
	}	
	
	private class SaveJob extends Job {

		public SaveJob() {
			super("Save bookmark problems");
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				BookmarkProblemsPersister persister = new BookmarkProblemsPersister(bookmarkProblemsFile);
				persister.save(bookmarkProblemsReference.get(), monitor);
				return Status.OK_STATUS;
			} catch (IOException e) {
				return new Status(IStatus.ERROR, BookmarksPlugin.PLUGIN_ID, 0, "Could save bookmark problems", e);
			} finally {
				monitor.done();
			}
		}

	}

}
