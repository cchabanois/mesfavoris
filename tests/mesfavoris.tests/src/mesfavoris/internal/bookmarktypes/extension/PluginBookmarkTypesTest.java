package mesfavoris.internal.bookmarktypes.extension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import mesfavoris.bookmarktype.BookmarkPropertyDescriptor;
import mesfavoris.bookmarktype.BookmarkPropertyDescriptor.BookmarkPropertyType;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.internal.bookmarktypes.extension.PluginBookmarkType.PrioritizedElement;
import mesfavoris.internal.markers.GotoFileMarkerBookmark;
import mesfavoris.model.Bookmark;

public class PluginBookmarkTypesTest {
	private PluginBookmarkTypes pluginBookmarkTypes;

	@Before
	public void setUp() {
		this.pluginBookmarkTypes = new PluginBookmarkTypes();
	}

	@Test
	public void testDefaultBookmarkType() {
		// When
		PluginBookmarkType bookmarkType = pluginBookmarkTypes.getBookmarkType("default");

		// Then
		assertNotNull(bookmarkType);
		assertEquals("default", bookmarkType.getName());
		assertEquals(0, getPrioritizedGotoBookmark(bookmarkType, GotoFileMarkerBookmark.class).getPriority());
		BookmarkPropertyDescriptor propertyDescriptor = bookmarkType.getPropertyDescriptor(Bookmark.PROPERTY_NAME);
		assertEquals(BookmarkPropertyType.STRING, propertyDescriptor.getType());
		assertFalse(propertyDescriptor.isUpdatable());

	}

	@Test
	public void testGetPropertyDescriptor() {
		// When
		BookmarkPropertyDescriptor propertyDescriptor = pluginBookmarkTypes
				.getPropertyDescriptor(Bookmark.PROPERTY_NAME);

		// Then
		assertEquals(BookmarkPropertyType.STRING, propertyDescriptor.getType());
		assertFalse(propertyDescriptor.isUpdatable());
	}

	private PrioritizedElement<IGotoBookmark> getPrioritizedGotoBookmark(PluginBookmarkType bookmarkType,
			Class<? extends IGotoBookmark> type) {
		for (PrioritizedElement<IGotoBookmark> gotoBookmark : bookmarkType.getGotoBookmarks()) {
			if (type.isInstance(gotoBookmark.getElement())) {
				return gotoBookmark;
			}
		}
		return null;
	}
}
