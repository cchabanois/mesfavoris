package mesfavoris.persistence.json;

import static mesfavoris.persistence.json.JsonSerializerConstants.NAME_BOOKMARKS;
import static mesfavoris.persistence.json.JsonSerializerConstants.NAME_CHILDREN;
import static mesfavoris.persistence.json.JsonSerializerConstants.NAME_ID;
import static mesfavoris.persistence.json.JsonSerializerConstants.NAME_PROPERTIES;
import static mesfavoris.persistence.json.JsonSerializerConstants.NAME_VERSION;
import static mesfavoris.persistence.json.JsonSerializerConstants.VERSION_1_0;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import com.google.gson.stream.JsonWriter;

import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.persistence.IBookmarksTreeSerializer;

/**
 * Serialize a {@link BookmarksTree}
 * 
 * @author cchabanois
 *
 */
public class BookmarksTreeJsonSerializer implements IBookmarksTreeSerializer {
	private final boolean indent;

	public BookmarksTreeJsonSerializer(boolean indent) {
		this.indent = indent;
	}

	@Override
	public void serialize(BookmarksTree bookmarksTree, BookmarkId bookmarkFolderId, Writer writer, IProgressMonitor monitor)
			throws IOException {
		JsonWriter jsonWriter = new JsonWriter(writer);
		if (indent) {
			jsonWriter.setIndent("  ");
		}
		try {
			serialize(bookmarksTree, bookmarkFolderId, jsonWriter, monitor);
		} finally {
			jsonWriter.close();
		}
	}

	private void serialize(BookmarksTree bookmarksTree, BookmarkId bookmarkFolderId, JsonWriter writer,
			IProgressMonitor monitor) throws IOException {
		writer.beginObject();
		writer.name(NAME_VERSION).value(VERSION_1_0);
		writer.name(NAME_BOOKMARKS);
		BookmarkFolder bookmarkFolder = (BookmarkFolder) bookmarksTree.getBookmark(bookmarkFolderId);
		serializeBookmarkFolder(writer, bookmarksTree, bookmarkFolder, monitor);
		writer.endObject();
	}

	private void serializeBookmarkFolder(JsonWriter writer, BookmarksTree bookmarksTree, BookmarkFolder bookmarkFolder,
			IProgressMonitor monitor) throws IOException {
		writer.beginObject();
		writer.name(NAME_ID).value(bookmarkFolder.getId().toString());
		writer.name(NAME_PROPERTIES);
		serializeProperties(writer, bookmarkFolder.getProperties(), monitor);
		writer.name(NAME_CHILDREN);
		serializeBookmarks(writer, bookmarksTree, bookmarksTree.getChildren(bookmarkFolder.getId()), monitor);
		writer.endObject();
	}

	private void serializeProperties(JsonWriter writer, Map<String, String> properties, IProgressMonitor monitor)
			throws IOException {
		writer.beginObject();
		for (Map.Entry<String, String> entry : properties.entrySet()) {
			writer.name(entry.getKey()).value(entry.getValue());
		}
		writer.endObject();
	}

	private void serializeBookmarks(JsonWriter writer, BookmarksTree bookmarksTree, List<Bookmark> bookmarks,
			IProgressMonitor monitor) throws IOException {
		writer.beginArray();
		for (Bookmark bookmark : bookmarks) {
			if (bookmark instanceof BookmarkFolder) {
				serializeBookmarkFolder(writer, bookmarksTree, (BookmarkFolder) bookmark, monitor);
			} else {
				serializeBookmark(writer, bookmarksTree, bookmark, monitor);
			}
		}
		writer.endArray();
	}

	private void serializeBookmark(JsonWriter writer, BookmarksTree bookmarksTree, Bookmark bookmark,
			IProgressMonitor monitor) throws IOException {
		writer.beginObject();
		writer.name(NAME_ID).value(bookmark.getId().toString());
		writer.name(NAME_PROPERTIES);
		serializeProperties(writer, bookmark.getProperties(), monitor);
		writer.endObject();
	}

}
