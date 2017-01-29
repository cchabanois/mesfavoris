package mesfavoris.internal.problems;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.collect.Maps;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import mesfavoris.model.BookmarkId;
import mesfavoris.problems.BookmarkProblem;
import mesfavoris.problems.BookmarkProblem.Severity;

public class BookmarkProblemsPersister {
	private static final String NAME_BOOKMARK_ID = "bookmarkId";
	private static final String NAME_PROBLEMS = "problems";
	private static final String NAME_PROBLEM_TYPE = "type";
	private static final String NAME_PROBLEM_SEVERITY = "severity";
	private static final String NAME_PROBLEM_PROPERTIES = "properties";

	private final File bookmarkProblemsFile;

	public BookmarkProblemsPersister(File bookmarkProblemsFile) {
		this.bookmarkProblemsFile = bookmarkProblemsFile;
	}

	public void save(BookmarkProblems bookmarkProblems, IProgressMonitor monitor) throws IOException {
		monitor.beginTask("Saving bookmark problems", bookmarkProblems.size());
		JsonWriter jsonWriter = new JsonWriter(new FileWriter(bookmarkProblemsFile));
		jsonWriter.setIndent("  ");
		try {
			jsonWriter.beginArray();
			for (BookmarkId bookmarkId : bookmarkProblems.getBookmarksWithProblems()) {
				jsonWriter.beginObject();
				jsonWriter.name(NAME_BOOKMARK_ID).value(bookmarkId.toString());
				jsonWriter.name(NAME_PROBLEMS);
				jsonWriter.beginArray();
				for (BookmarkProblem bookmarkProblem : bookmarkProblems.getBookmarkProblems(bookmarkId)) {
					jsonWriter.beginObject();
					jsonWriter.name(NAME_PROBLEM_TYPE).value(bookmarkProblem.getProblemType());
					jsonWriter.name(NAME_PROBLEM_SEVERITY).value(bookmarkProblem.getSeverity().name());
					jsonWriter.name(NAME_PROBLEM_PROPERTIES);
					jsonWriter.beginArray();
					for (Map.Entry<String, String> entry : bookmarkProblem.getProperties().entrySet()) {
						jsonWriter.beginObject();
						jsonWriter.name(entry.getKey());
						jsonWriter.value(entry.getValue());
						jsonWriter.endObject();
					}
					jsonWriter.endArray();
					jsonWriter.endObject();
					monitor.worked(1);
				}
				jsonWriter.endArray();
				jsonWriter.endObject();
			}
			jsonWriter.endArray();

		} finally {
			jsonWriter.close();
		}
	}

	public BookmarkProblems load() throws IOException {
		BookmarkProblems bookmarkProblems = new BookmarkProblems();
		if (!bookmarkProblemsFile.exists()) {
			return bookmarkProblems;
		}
		JsonReader jsonReader = new JsonReader(new FileReader(bookmarkProblemsFile));
		try {
			jsonReader.beginArray();
			while (jsonReader.hasNext()) {
				bookmarkProblems = deserialiseProblemsForBookmark(bookmarkProblems, jsonReader);
			}
			jsonReader.endArray();
			return bookmarkProblems;
		} finally {
			jsonReader.close();
		}
	}

	private BookmarkProblems deserialiseProblemsForBookmark(BookmarkProblems bookmarkProblems, JsonReader jsonReader)
			throws IOException {
		jsonReader.beginObject();
		BookmarkId bookmarkId = null;
		while (jsonReader.hasNext()) {
			String name = jsonReader.nextName();
			if (NAME_BOOKMARK_ID.equals(name)) {
				bookmarkId = new BookmarkId(jsonReader.nextString());
			}
			if (NAME_PROBLEMS.equals(name)) {
				jsonReader.beginArray();
				while (jsonReader.hasNext()) {
					BookmarkProblem bookmarkProblem = deserializeBookmarkProblem(jsonReader, bookmarkId);
					if (bookmarkProblem != null) {
						bookmarkProblems = bookmarkProblems.add(bookmarkProblem);
					}
				}
				jsonReader.endArray();
			}
		}
		jsonReader.endObject();
		return bookmarkProblems;
	}

	private BookmarkProblem deserializeBookmarkProblem(JsonReader jsonReader, BookmarkId bookmarkId)
			throws IOException {
		String name;
		String problemType = null;
		Severity severity = Severity.WARNING;
		Map<String, String> properties = Maps.newHashMap();
		jsonReader.beginObject();
		while (jsonReader.hasNext()) {
			name = jsonReader.nextName();
			if (NAME_PROBLEM_TYPE.equals(name)) {
				problemType = jsonReader.nextString();
			}
			if (NAME_PROBLEM_SEVERITY.equals(name)) {
				severity = Severity.valueOf(jsonReader.nextString());
			}
			if (NAME_PROBLEM_PROPERTIES.equals(name)) {
				properties.putAll(deserializeProperties(jsonReader));
			}
		}
		jsonReader.endObject();
		if (bookmarkId == null || problemType == null) {
			return null;
		}
		return new BookmarkProblem(bookmarkId, problemType, severity, properties);
	}

	private Map<String, String> deserializeProperties(JsonReader jsonReader) throws IOException {
		Map<String, String> properties = Maps.newHashMap();
		jsonReader.beginArray();
		while (jsonReader.hasNext()) {
			jsonReader.beginObject();
			String name = jsonReader.nextName();
			String value = jsonReader.nextString();
			properties.put(name, value);
			jsonReader.endObject();
		}
		jsonReader.endArray();
		return properties;
	}

}
