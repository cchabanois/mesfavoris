package mesfavoris.texteditor.placeholders;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import mesfavoris.BookmarksPlugin;
import mesfavoris.StatusHelper;

public class PathPlaceholdersStore implements IPathPlaceholders {
	private final Map<String, PathPlaceholder> mappings = new ConcurrentHashMap<>();
	private final SaveJob saveJob = new SaveJob();
	private final File storeFile;

	public PathPlaceholdersStore(File storeFile) {
		this.storeFile = storeFile;
	}

	public void add(PathPlaceholder pathPlaceholder) {
		if (!pathPlaceholder.equals(mappings.put(pathPlaceholder.getName(), pathPlaceholder))) {
			saveJob.schedule();
		}
	}

	@Override
	public Iterator<PathPlaceholder> iterator() {
		return Collections.unmodifiableCollection(mappings.values()).iterator();
	}

	public Collection<PathPlaceholder> getMappings() {
		return Collections.unmodifiableCollection(mappings.values());
	}

	public PathPlaceholder get(String name) {
		return mappings.get(name);
	}

	public void remove(String name) {
		if (mappings.remove(name) != null) {
			saveJob.schedule();
		}
	}

	public void init() {
		try {
			load();
		} catch (IOException e) {
			StatusHelper.logError("Could not load path placeholders", e);
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
				mappings.put(name, new PathPlaceholder(name, Path.fromPortableString(value)));
			}
			jsonReader.endObject();
		} finally {
			jsonReader.close();
		}
	}

	private void save(IProgressMonitor monitor) throws IOException {
		monitor.beginTask("Saving path placeholders", mappings.size());
		JsonWriter jsonWriter = new JsonWriter(new FileWriter(storeFile));
		jsonWriter.setIndent("  ");
		try {
			jsonWriter.beginObject();
			for (PathPlaceholder pathPlaceholder : mappings.values()) {
				jsonWriter.name(pathPlaceholder.getName());
				jsonWriter.value(pathPlaceholder.getPath().toPortableString());
				monitor.worked(1);
			}
			jsonWriter.endObject();

		} finally {
			jsonWriter.close();
		}
	}

	private class SaveJob extends Job {

		public SaveJob() {
			super("Save path placeholders");
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				save(monitor);
				return Status.OK_STATUS;
			} catch (IOException e) {
				return new Status(IStatus.ERROR, BookmarksPlugin.PLUGIN_ID, 0, "Could not save path placeholders", e);
			} finally {
				monitor.done();
			}
		}

	}
}
