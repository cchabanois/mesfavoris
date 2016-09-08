package mesfavoris.gdrive.mappings;

import static mesfavoris.remote.RemoteBookmarkFolder.PROP_READONLY;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
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
	private static final String PROP_SHARING_USER = "sharingUser";
	private final IBookmarkMappingsPersister bookmarkMappingsPersister;
	private final Map<BookmarkId, BookmarkMapping> mappings = new ConcurrentHashMap<>();
	private final SaveJob saveJob = new SaveJob();
	private final ListenerList listenerList = new ListenerList();

	public BookmarkMappingsStore(IBookmarkMappingsPersister bookmarkMappingsPersister) {
		this.bookmarkMappingsPersister = bookmarkMappingsPersister;
	}

	public void add(BookmarkId bookmarkFolderId, File file) {
		if (add(new BookmarkMapping(bookmarkFolderId, file.getId(), getProperties(file)))) {
			fireMappingAdded(bookmarkFolderId);
			saveJob.schedule();
		}
	}

	private boolean add(BookmarkMapping bookmarkMapping) {
		return mappings.put(bookmarkMapping.getBookmarkFolderId(), bookmarkMapping) == null;
	}

	private void replace(BookmarkMapping bookmarkMapping) {
		mappings.replace(bookmarkMapping.getBookmarkFolderId(), bookmarkMapping);
	}

	public void update(File file) {
		Optional<BookmarkMapping> mapping = getMapping(file.getId());
		if (!mapping.isPresent()) {
			return;
		}
		Map<String, String> properties = getProperties(file);
		if (!properties.equals(mapping.get().getProperties())) {
			replace(new BookmarkMapping(mapping.get().getBookmarkFolderId(), mapping.get().getFileId(), properties));
			saveJob.schedule();
		}
	}

	private Map<String, String> getProperties(File file) {
		Map<String, String> properties = new HashMap<>();
		if (Boolean.FALSE.equals(file.getEditable())) {
			properties.put(PROP_READONLY, Boolean.TRUE.toString());
		}
		if (file.getSharingUser() != null) {
			properties.put(PROP_SHARING_USER, file.getSharingUser().getDisplayName());
		}
		return properties;
	}

	public Optional<BookmarkMapping> getMapping(BookmarkId bookmarkFolderId) {
		return mappings.values().stream().filter(mapping -> mapping.getBookmarkFolderId().equals(bookmarkFolderId))
				.findAny();
	}

	public Optional<BookmarkMapping> getMapping(String fileId) {
		return mappings.values().stream().filter(mapping -> mapping.getFileId().equals(fileId)).findAny();
	}

	public Set<BookmarkMapping> getMappings() {
		return ImmutableSet.copyOf(mappings.values());
	}

	public void remove(BookmarkId bookmarkFolderId) {
		if (mappings.remove(bookmarkFolderId) != null) {
			fireMappingRemoved(bookmarkFolderId);
			saveJob.schedule();
		}
	}

	public void init() {
		try {
			mappings.clear();
			bookmarkMappingsPersister.load().forEach(mapping -> add(mapping));
		} catch (IOException e) {
			StatusHelper.logError("Could not load bookmark mappings", e);
		}
	}

	public void close() throws InterruptedException {
		saveJob.join();
	}

	@Override
	public void bookmarksModified(List<BookmarksModification> modifications) {
		Set<BookmarkMapping> mappingsToRemove = modifications.stream()
				.filter(modification -> modification instanceof BookmarkDeletedModification)
				.map(modification -> (BookmarkDeletedModification) modification)
				.map(modification -> getDeletedMappings(modification))
				.reduce(new HashSet<BookmarkMapping>(), (mappingsSet, modificationMappingsSet) -> {
					mappingsSet.addAll(modificationMappingsSet);
					return mappingsSet;
				});
		mappingsToRemove.forEach(mapping -> remove(mapping.getBookmarkFolderId()));
	}

	private Set<BookmarkMapping> getDeletedMappings(BookmarkDeletedModification modification) {
		return mappings.values().stream()
				.filter(mapping -> modification.getTargetTree().getBookmark(mapping.getBookmarkFolderId()) == null)
				.collect(Collectors.toSet());
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
				bookmarkMappingsPersister.save(new HashSet<>(mappings.values()), monitor);
				return Status.OK_STATUS;
			} catch (IOException e) {
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Could save GDrive bookmarks store", e);
			} finally {
				monitor.done();
			}
		}

	}

}
