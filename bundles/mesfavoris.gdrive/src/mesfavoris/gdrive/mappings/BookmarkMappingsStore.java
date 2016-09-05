package mesfavoris.gdrive.mappings;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.google.api.services.drive.model.File;
import com.google.common.collect.ImmutableSet;

import mesfavoris.gdrive.Activator;
import mesfavoris.gdrive.StatusHelper;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.IBookmarksListener;
import mesfavoris.model.modification.BookmarkDeletedModification;
import mesfavoris.model.modification.BookmarksModification;

/**
 * Store mappings between bookmark folders and remote files that are storing
 * them
 * 
 * @author cchabanois
 *
 */
public class BookmarkMappingsStore implements IBookmarksListener, IBookmarkMappings {
	private final IBookmarkMappingsPersister bookmarkMappingsPersister;
	private final Set<BookmarkMapping> mappings = ConcurrentHashMap.newKeySet();
	private final SaveJob saveJob = new SaveJob();
	private final ListenerList listenerList = new ListenerList();

	public BookmarkMappingsStore(IBookmarkMappingsPersister bookmarkMappingsPersister) {
		this.bookmarkMappingsPersister = bookmarkMappingsPersister;
	}

	public void add(BookmarkId bookmarkFolderId, File file) {
		if (mappings.add(new BookmarkMapping(bookmarkFolderId, file.getId(), getProperties(file)))) {
			fireMappingAdded(bookmarkFolderId);
			saveJob.schedule();
		}
	}

	private Map<String, String> getProperties(File file) {
		Map<String,String> properties = new HashMap<>();
		if (Boolean.FALSE.equals(file.getEditable())) {
			properties.put("readonly", Boolean.TRUE.toString());
		}
		if (file.getSharingUser() != null) {
			properties.put("sharingUser", file.getSharingUser().getDisplayName());
		}
		return properties;
	}

	public Optional<BookmarkMapping> getMapping(BookmarkId bookmarkFolderId) {
		return mappings.stream().filter(mapping -> mapping.getBookmarkFolderId().equals(bookmarkFolderId)).findAny();
	}

	public Optional<BookmarkMapping> getMapping(String fileId) {
		return mappings.stream().filter(mapping -> mapping.getFileId().equals(fileId)).findAny();
	}
	
	public Set<BookmarkMapping> getMappings() {
		return ImmutableSet.copyOf(mappings);
	}

	public void remove(BookmarkId bookmarkFolderId) {
		if (mappings.removeIf(mapping -> mapping.getBookmarkFolderId().equals(bookmarkFolderId))) {
			fireMappingRemoved(bookmarkFolderId);
			saveJob.schedule();
		}
	}

	public void init() {
		try {
			mappings.clear();
			mappings.addAll(bookmarkMappingsPersister.load());
		} catch (IOException e) {
			StatusHelper.logError("Could not load bookmark mappings", e);
		}
	}

	public void close() throws InterruptedException {
		saveJob.join();
	}

	@Override
	public void bookmarksModified(List<BookmarksModification> modifications) {
		for (BookmarkId bookmarkFolderId : getDeletedMappedBookmarkFolders(modifications)) {
			remove(bookmarkFolderId);
		}
	}

	private List<BookmarkId> getDeletedMappedBookmarkFolders(List<BookmarksModification> events) {
		return events.stream().filter(p -> p instanceof BookmarkDeletedModification)
				.map(p -> (BookmarkDeletedModification) p).filter(p -> getMapping(p.getBookmarkId()).isPresent())
				.map(p -> p.getBookmarkId()).collect(Collectors.toList());

	}

	public void addListener(IBookmarkMappingsListener listener) {
		listenerList.add(listener);
	}

	public void removeListener(IBookmarkMappingsListener listener) {
		listenerList.remove(listener);
	}

	private void fireMappingAdded(BookmarkId bookmarkFolderId) {
		Object[] listeners = listenerList.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			final IBookmarkMappingsListener listener = (IBookmarkMappingsListener) listeners[i];
			SafeRunner.run(new ISafeRunnable() {

				public void run() throws Exception {
					listener.mappingAdded(bookmarkFolderId);

				}

				public void handleException(Throwable exception) {
					StatusHelper.logError("Error when mapping added", exception);
				}
			});
		}
	}

	private void fireMappingRemoved(BookmarkId bookmarkFolderId) {
		Object[] listeners = listenerList.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			final IBookmarkMappingsListener listener = (IBookmarkMappingsListener) listeners[i];
			SafeRunner.run(new ISafeRunnable() {

				public void run() throws Exception {
					listener.mappingRemoved(bookmarkFolderId);

				}

				public void handleException(Throwable exception) {
					StatusHelper.logError("Error when mapping removed", exception);
				}
			});
		}
	}

	private class SaveJob extends Job {

		public SaveJob() {
			super("Save bookmark mappings");
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				bookmarkMappingsPersister.save(mappings, monitor);
				return Status.OK_STATUS;
			} catch (IOException e) {
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Could save GDrive bookmarks store", e);
			} finally {
				monitor.done();
			}
		}

	}

}
