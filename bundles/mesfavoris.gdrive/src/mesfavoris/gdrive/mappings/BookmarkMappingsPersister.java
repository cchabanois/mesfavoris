package mesfavoris.gdrive.mappings;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import mesfavoris.model.BookmarkId;

public class BookmarkMappingsPersister implements IBookmarkMappingsPersister {
	private static final String PROPERTY_FILE_ID = "fileId";
	private final File storeFile;
	
	public BookmarkMappingsPersister(File storeFile) {
		this.storeFile = storeFile;
	}
	
	@Override
	public Set<BookmarkMapping> load() throws IOException {
		if (!storeFile.exists()) {
			return Collections.emptySet();
		}
		Set<BookmarkMapping> mappings = new HashSet<>();
		JsonReader jsonReader = new JsonReader(new FileReader(storeFile));
		try {
			jsonReader.beginObject();
			while (jsonReader.hasNext()) {
				String name = jsonReader.nextName();
				String fileId = null;
				Map<String, String> properties;
				JsonToken jsonToken = jsonReader.peek();
				if (jsonToken == JsonToken.BEGIN_OBJECT) {
					properties = new HashMap<>();
					jsonReader.beginObject();
					while (jsonReader.hasNext()) {
						String propName = jsonReader.nextName();
						String propValue = jsonReader.nextString();
						if (PROPERTY_FILE_ID.equals(propName)) {
							fileId = propValue;
						} else {
							properties.put(propName, propValue);
						}
					}
					jsonReader.endObject();
				} else {
					// old format
					fileId = jsonReader.nextString();
					properties = Collections.emptyMap();
				}
				BookmarkId bookmarkId = new BookmarkId(name);
				mappings.add(new BookmarkMapping(bookmarkId, fileId, properties));
			}
			jsonReader.endObject();
			return mappings;
		} finally {
			jsonReader.close();
		}
	}

	@Override
	public void save(Set<BookmarkMapping> mappings, IProgressMonitor monitor) throws IOException {
		monitor.beginTask("Saving bookmark mappings", mappings.size());
		JsonWriter jsonWriter = new JsonWriter(new FileWriter(storeFile));
		jsonWriter.setIndent("  ");
		try {
			jsonWriter.beginObject();
			for (BookmarkMapping mapping : mappings) {
				jsonWriter.name(mapping.getBookmarkFolderId().toString());
				jsonWriter.beginObject();
				jsonWriter.name(PROPERTY_FILE_ID);
				jsonWriter.value(mapping.getFileId());
				for (Map.Entry<String, String> entry : mapping.getProperties().entrySet()) {
					jsonWriter.name(entry.getKey());
					jsonWriter.value(entry.getValue());
				}
				jsonWriter.endObject();
				monitor.worked(1);
			}
			jsonWriter.endObject();

		} finally {
			jsonWriter.close();
		}
	}	
	
	
}
