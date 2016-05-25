package mesfavoris.gdrive.mappings;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.model.IBookmarksListener;
import org.chabanois.mesfavoris.model.modification.BookmarkDeletedModification;
import org.chabanois.mesfavoris.model.modification.BookmarksModification;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import mesfavoris.gdrive.Activator;
import mesfavoris.gdrive.StatusHelper;

/**
 * Store mappings between bookmark folders and remote files that are storing
 * them
 * 
 * @author cchabanois
 *
 */
public class BookmarkMappingsStore implements IBookmarksListener {
	private final File storeFile;
	private final Map<BookmarkId, String> mappings = new ConcurrentHashMap<BookmarkId, String>();
	private final SaveJob saveJob = new SaveJob();
	private final ListenerList listenerList = new ListenerList();
	
	public BookmarkMappingsStore(File storeFile) {
		this.storeFile = storeFile;
	}

	public void add(BookmarkId bookmarkFolderId, String fileId) {
		if (!fileId.equals(mappings.put(bookmarkFolderId, fileId))) {
			fireMappingAdded(bookmarkFolderId);
			saveJob.schedule();
		}
	}

	public String getFileId(BookmarkId bookmarkFolderId) {
		return mappings.get(bookmarkFolderId);
	}

	public Set<BookmarkId> getBookmarkFolderIds() {
		return new HashSet<BookmarkId>(mappings.keySet());
	}

	public void remove(BookmarkId bookmarkFolderId) {
		if (mappings.remove(bookmarkFolderId) != null) {
			fireMappingRemoved(bookmarkFolderId);
			saveJob.schedule();
		}
	}

	public void init() {
		try {
			load();
		} catch (IOException e) {
			StatusHelper.logError("Could not load bookmark mappings", e);
		}
	}

	public void close() throws InterruptedException {
		saveJob.join();
	}

	private void load() throws IOException {
		if (!storeFile.exists()) {
			return;
		}
		mappings.clear();
		JsonReader jsonReader = new JsonReader(new FileReader(storeFile));
		try {
			jsonReader.beginObject();
			while (jsonReader.hasNext()) {
				String name = jsonReader.nextName();
				String value = jsonReader.nextString();
				mappings.put(new BookmarkId(name), value);
			}
			jsonReader.endObject();
		} finally {
			jsonReader.close();
		}
	}

	private void save(IProgressMonitor monitor) throws IOException {
		monitor.beginTask("Saving bookmark mappings", mappings.size());
		JsonWriter jsonWriter = new JsonWriter(new FileWriter(storeFile));
		jsonWriter.setIndent("  ");
		try {
			jsonWriter.beginObject();
			for (Map.Entry<BookmarkId, String> mapping : mappings.entrySet()) {
				jsonWriter.name(mapping.getKey().toString());
				jsonWriter.value(mapping.getValue());
				monitor.worked(1);
			}
			jsonWriter.endObject();

		} finally {
			jsonWriter.close();
		}
	}

	@Override
	public void bookmarksModified(List<BookmarksModification> modifications) {
		for (BookmarkId bookmarkFolderId : getDeletedMappedBookmarkFolders(modifications)) {
			remove(bookmarkFolderId);
		}
	}

	private List<BookmarkId> getDeletedMappedBookmarkFolders(List<BookmarksModification> events) {
		return events.stream().filter(p -> p instanceof BookmarkDeletedModification)
				.map(p -> (BookmarkDeletedModification) p).filter(p -> mappings.containsKey(p.getBookmarkId()))
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
				save(monitor);
				return Status.OK_STATUS;
			} catch (IOException e) {
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Could save GDrive bookmarks store", e);
			} finally {
				monitor.done();
			}
		}

	}

}
