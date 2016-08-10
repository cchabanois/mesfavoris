package mesfavoris.internal.model.compare;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import mesfavoris.internal.model.compare.BookmarkComparer.BookmarkDifferences;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;

public class BookmarkComparerTest {
	private Bookmark bookmarkSource;
	private final BookmarkComparer bookmarkComparer = new BookmarkComparer();

	@Before
	public void setUp() {
		bookmarkSource = new Bookmark(new BookmarkId(),
				ImmutableMap.of("key1", "value1", "key2", "value2", "key3", "value3", "key4", "value4"));
	}

	@Test
	public void testCompare() {
		// Given
		Map<String, String> properties = Maps.newHashMap(bookmarkSource.getProperties());
		properties.remove("key2");
		properties.put("key5", "value5");
		properties.put("key3", "value3 modified");
		Bookmark bookmarkTarget = new Bookmark(bookmarkSource.getId(), properties);

		// When
		BookmarkDifferences diff = bookmarkComparer.compare(bookmarkSource, bookmarkTarget);
		
		// Then
		assertThat(diff.getAddedProperties()).containsExactly("key5");
		assertThat(diff.getDeletedProperties()).containsExactly("key2");
		assertThat(diff.getModifiedProperties()).containsExactly("key3");
	}

}
