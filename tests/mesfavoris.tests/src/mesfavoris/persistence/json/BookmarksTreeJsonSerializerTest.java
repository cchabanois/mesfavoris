package mesfavoris.persistence.json;

import static mesfavoris.testutils.BookmarksTreeTestUtil.getBookmarkFolder;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;
import org.junit.Test;

import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.persistence.json.BookmarksTreeJsonDeserializer;
import mesfavoris.persistence.json.BookmarksTreeJsonSerializer;
import mesfavoris.testutils.BookmarksTreeBuilder;
import mesfavoris.testutils.IncrementalIDGenerator;

public class BookmarksTreeJsonSerializerTest {
	private BookmarksTreeJsonSerializer bookmarksTreeJsonSerializer;

	@Before
	public void setUp() {
		bookmarksTreeJsonSerializer = new BookmarksTreeJsonSerializer(true);
	}

	@Test
	public void testSerializeBookmarksTree() throws IOException {
		// Given
		BookmarksTree bookmarksTree = new BookmarksTreeBuilder(new IncrementalIDGenerator(), 5, 3, 2).build();

		// When
		String result = serialize(bookmarksTree, bookmarksTree.getRootFolder().getId());

		// Then
		assertEquals(bookmarksTree.toString(), deserialize(result).toString());
	}

	@Test
	public void testSerializeBookmarksSubTree() throws IOException {
		// Given
		BookmarksTree bookmarksTree = new BookmarksTreeBuilder(new IncrementalIDGenerator(), 5, 3, 2).build();
		BookmarkFolder parentFolder = getBookmarkFolder(bookmarksTree, 0, 0);

		// When
		String result = serialize(bookmarksTree, parentFolder.getId());

		// Then
		assertEquals(bookmarksTree.subTree(parentFolder.getId()).toString(), deserialize(result).toString());
	}

	private String serialize(BookmarksTree bookmarksTree, BookmarkId bookmarkFolderId) throws IOException {
		StringWriter writer = new StringWriter();
		bookmarksTreeJsonSerializer.serialize(bookmarksTree, bookmarkFolderId, writer, new NullProgressMonitor());
		return writer.toString();
	}

	private BookmarksTree deserialize(String serializedBookmarks) throws IOException {
		BookmarksTreeJsonDeserializer deserializer = new BookmarksTreeJsonDeserializer();
		return deserializer.deserialize(new StringReader(serializedBookmarks), new NullProgressMonitor());
	}

}
