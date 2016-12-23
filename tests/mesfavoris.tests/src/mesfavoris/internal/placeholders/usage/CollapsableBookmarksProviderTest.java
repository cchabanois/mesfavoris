package mesfavoris.internal.placeholders.usage;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Path;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;
import mesfavoris.placeholders.IPathPlaceholderResolver;
import mesfavoris.placeholders.PathPlaceholder;
import mesfavoris.placeholders.PathPlaceholderResolver;
import mesfavoris.placeholders.PathPlaceholdersMap;
import mesfavoris.texteditor.TextEditorBookmarkProperties;

public class CollapsableBookmarksProviderTest {
	private final PathPlaceholdersMap mappings = new PathPlaceholdersMap();
	private final IPathPlaceholderResolver pathPlaceholderResolver = new PathPlaceholderResolver(mappings);

	@Test
	public void testCollapsableBookmarks() {
		// Given
		mappings.add(new PathPlaceholder("HOME", new Path("/home/cchabanois")));
		mappings.add(new PathPlaceholder("BLT", new Path("/home/cchabanois/blt")));
		Bookmark bookmark1 = bookmark("bookmark1", "/home/cchabanois/blt/file.txt");
		Bookmark bookmark2 = bookmark("bookmark2", "${HOME}/blt/file2.txt");
		Bookmark bookmark3 = bookmark("bookmark3", "${HOME}/file3.txt");
		CollapsableBookmarksProvider collapsableBookmarksProvider = new CollapsableBookmarksProvider(
				pathPlaceholderResolver, "BLT");

		// When
		List<Bookmark> collapsableBookmarks = collapsableBookmarksProvider
				.getCollapsableBookmarks(Lists.newArrayList(bookmark1, bookmark2, bookmark3));

		// Then
		assertThat(collapsableBookmarks).containsExactly(bookmark1, bookmark2);
	}

	@Test
	public void testAlreadyCollapsedBookmarkIsNotCollapsable() {
		// Given
		mappings.add(new PathPlaceholder("HOME", new Path("/home/cchabanois")));
		Bookmark bookmark3 = bookmark("bookmark3", "${HOME}/file3.txt");
		CollapsableBookmarksProvider collapsableBookmarksProvider = new CollapsableBookmarksProvider(
				pathPlaceholderResolver, "HOME");

		// When
		List<Bookmark> collapsableBookmarks = collapsableBookmarksProvider
				.getCollapsableBookmarks(Lists.newArrayList(bookmark3));

		// Then
		assertThat(collapsableBookmarks).isEmpty();
	}

	private Bookmark bookmark(String name, String filePath) {
		Map<String, String> properties = Maps.newHashMap();
		properties.put(Bookmark.PROPERTY_NAME, name);
		properties.put(TextEditorBookmarkProperties.PROP_FILE_PATH, filePath);
		return new Bookmark(new BookmarkId(), properties);
	}

}
