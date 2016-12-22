package mesfavoris.internal.recent;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.services.events.IEventBroker;

import com.google.common.collect.ImmutableMap;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import mesfavoris.BookmarksPlugin;
import mesfavoris.internal.StatusHelper;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.model.IBookmarksListener;
import mesfavoris.model.modification.BookmarkDeletedModification;
import mesfavoris.model.modification.BookmarksAddedModification;
import mesfavoris.model.modification.BookmarksModification;
import mesfavoris.topics.BookmarksEvents;

public class RecentBookmarksDatabase {
	private static final String NAME_DATE = "date";
	private static final String NAME_BOOKMARK_ID = "bookmarkId";
	private final AtomicReference<RecentBookmarks> recentBookmarksReference;
	private final File recentBookmarksFile;
	private final BookmarkDatabase bookmarkDatabase;
	private final SaveJob saveJob = new SaveJob();
	private final IEventBroker eventBroker;
	private final IBookmarksListener bookmarksListener;

	public RecentBookmarksDatabase(IEventBroker eventBroker, BookmarkDatabase bookmarkDatabase,
			File recentBookmarksFile, Duration recentDuration) {
		this.eventBroker = eventBroker;
		this.bookmarkDatabase = bookmarkDatabase;
		this.recentBookmarksFile = recentBookmarksFile;
		bookmarksListener = modifications -> {
			bookmarksAddedOrDeleted(getAddedBookmarks(modifications), getDeletedBookmarks(modifications));
		};
		recentBookmarksReference = new AtomicReference<RecentBookmarks>(new RecentBookmarks(recentDuration));
	}

	public void init() {
		try {
			load();
		} catch (Exception e) {
			StatusHelper.logError("Could not load recent bookmarks", e);
		}
		bookmarkDatabase.addListener(bookmarksListener);
	}

	public void close() throws InterruptedException {
		bookmarkDatabase.removeListener(bookmarksListener);
		do {
			saveJob.join();
		} while (saveJob.getState() != Job.NONE);
	}

	public RecentBookmarks getRecentBookmarks() {
		return recentBookmarksReference.get();
	}

	private void bookmarksAddedOrDeleted(List<BookmarkId> addedBookmarkIds, List<BookmarkId> deletedBookmarkIds) {
		if (deletedBookmarkIds.isEmpty() && addedBookmarkIds.isEmpty()) {
			return;
		}
		BookmarksTree bookmarksTree = bookmarkDatabase.getBookmarksTree();
		RecentBookmarks recentBookmarks;
		RecentBookmarks newRecentBookmarks;
		do {
			recentBookmarks = recentBookmarksReference.get();
			newRecentBookmarks = recentBookmarks;
			for (BookmarkId bookmarkId : addedBookmarkIds) {
				Optional<RecentBookmark> recentBookmark = createRecentBookmark(bookmarksTree, bookmarkId);
				if (recentBookmark.isPresent()) {
					newRecentBookmarks = newRecentBookmarks.add(recentBookmark.get());
				}
			}
			for (BookmarkId bookmarkId : deletedBookmarkIds) {
				newRecentBookmarks = newRecentBookmarks.delete(bookmarkId);
			}
			if (recentBookmarks == newRecentBookmarks) {
				return;
			}
		} while (!recentBookmarksReference.compareAndSet(recentBookmarks, newRecentBookmarks));
		postRecentBookmarksChanged(recentBookmarks, newRecentBookmarks);
		saveJob.schedule();
	}

	private Optional<RecentBookmark> createRecentBookmark(BookmarksTree bookmarksTree, BookmarkId bookmarkId) {
		Bookmark bookmark = bookmarksTree.getBookmark(bookmarkId);
		if (bookmark == null || bookmark.getPropertyValue(Bookmark.PROPERTY_CREATED) == null) {
			return Optional.empty();
		}
		try {
			Instant instantAdded = Instant.parse(bookmark.getPropertyValue(Bookmark.PROPERTY_CREATED));
			return Optional.of(new RecentBookmark(bookmarkId, instantAdded));
		} catch (DateTimeParseException e) {
			return Optional.empty();
		}
	}

	private void postRecentBookmarksChanged(RecentBookmarks previousRecentBookmarks,
			RecentBookmarks newRecentBookmarks) {
		eventBroker.post(BookmarksEvents.TOPIC_RECENT_BOOKMARKS_CHANGED,
				ImmutableMap.of("before", previousRecentBookmarks, "after", newRecentBookmarks));
	}

	private void save(IProgressMonitor monitor) throws IOException {
		RecentBookmarks recentBookmarks = recentBookmarksReference.get();
		monitor.beginTask("Saving recent bookmarks", recentBookmarks.size());
		JsonWriter jsonWriter = new JsonWriter(new FileWriter(recentBookmarksFile));
		jsonWriter.setIndent("  ");
		try {
			jsonWriter.beginArray();
			for (RecentBookmark recentBookmark : recentBookmarks.getSet()) {
				jsonWriter.beginObject();
				jsonWriter.name(NAME_BOOKMARK_ID).value(recentBookmark.getBookmarkId().toString());
				jsonWriter.name(NAME_DATE).value(recentBookmark.getInstantAdded().toString());
				jsonWriter.endObject();
				monitor.worked(1);
			}
			jsonWriter.endArray();

		} finally {
			jsonWriter.close();
		}
	}

	private void load() throws IOException {
		if (!recentBookmarksFile.exists()) {
			return;
		}
		BookmarksTree bookmarksTree = bookmarkDatabase.getBookmarksTree();
		RecentBookmarks recentBookmarks = new RecentBookmarks(recentBookmarksReference.get().getRecentDuration());
		JsonReader jsonReader = new JsonReader(new FileReader(recentBookmarksFile));
		try {
			jsonReader.beginArray();
			while (jsonReader.hasNext()) {
				jsonReader.beginObject();
				BookmarkId bookmarkId = null;
				Instant instantAdded = Instant.now();
				while (jsonReader.hasNext()) {
					String name = jsonReader.nextName();
					if (NAME_BOOKMARK_ID.equals(name)) {
						bookmarkId = new BookmarkId(jsonReader.nextString());
					}
					if (NAME_DATE.equals(name)) {
						instantAdded = Instant.parse(jsonReader.nextString());
					}
				}
				jsonReader.endObject();
				if (bookmarksTree.getBookmark(bookmarkId) != null) {
					RecentBookmark recentBookmark = new RecentBookmark(bookmarkId, instantAdded);
					recentBookmarks = recentBookmarks.add(recentBookmark);
				}
			}
			jsonReader.endArray();
			recentBookmarksReference.set(recentBookmarks);
		} finally {
			jsonReader.close();
		}
	}

	private List<BookmarkId> getDeletedBookmarks(List<BookmarksModification> modifications) {
		return modifications.stream().filter(modification -> modification instanceof BookmarkDeletedModification)
				.map(modification -> (BookmarkDeletedModification) modification)
				.flatMap(modification -> StreamSupport.stream(modification.getDeletedBookmarks().spliterator(), false))
				.map(bookmark -> bookmark.getId()).collect(Collectors.toList());
	}

	private List<BookmarkId> getAddedBookmarks(List<BookmarksModification> modifications) {
		return modifications.stream().filter(modification -> modification instanceof BookmarksAddedModification)
				.map(modification -> (BookmarksAddedModification) modification)
				.flatMap(modification -> modification.getBookmarks().stream()).map(bookmark -> bookmark.getId())
				.collect(Collectors.toList());
	}

	private class SaveJob extends Job {

		public SaveJob() {
			super("Save recently added bookmarks");
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				save(monitor);
				return Status.OK_STATUS;
			} catch (IOException e) {
				return new Status(IStatus.ERROR, BookmarksPlugin.PLUGIN_ID, 0, "Could save recently added bookmarks",
						e);
			} finally {
				monitor.done();
			}
		}

	}

}
