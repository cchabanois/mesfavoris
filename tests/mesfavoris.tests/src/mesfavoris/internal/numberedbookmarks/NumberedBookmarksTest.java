package mesfavoris.internal.numberedbookmarks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.internal.preferences.EclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import mesfavoris.model.BookmarkId;
import mesfavoris.topics.BookmarksEvents;

public class NumberedBookmarksTest {
	private NumberedBookmarks numberedBookmarks;
	private IEventBroker eventBroker = mock(IEventBroker.class);
	private IEclipsePreferences eclipsePreferences;

	@Before
	public void setUp() {
		eclipsePreferences = new EclipsePreferences();
		numberedBookmarks = new NumberedBookmarks(eclipsePreferences, eventBroker);
		numberedBookmarks.init();
	}

	@After
	public void tearDown() {
		numberedBookmarks.close();
	}

	@Test
	public void testSetNumberedBookmark() {
		// Given
		BookmarkId bookmarkId = new BookmarkId("myBookmark");

		// When
		numberedBookmarks.set(BookmarkNumber.ONE, bookmarkId);

		// Then
		assertEquals(bookmarkId, numberedBookmarks.getBookmark(BookmarkNumber.ONE).get());
		assertEquals(BookmarkNumber.ONE, numberedBookmarks.getBookmarkNumber(bookmarkId).get());
		verify(eventBroker).post(BookmarksEvents.TOPIC_NUMBERED_BOOKMARKS_CHANGED,
				ImmutableMap.of("bookmarkId", "myBookmark", "bookmarkNumber", "ONE"));
	}

	@Test
	public void testRemoveNumberedBookmark() {
		// Given
		BookmarkId bookmarkId = new BookmarkId("myBookmark");
		numberedBookmarks.set(BookmarkNumber.ONE, bookmarkId);

		// When
		numberedBookmarks.remove(BookmarkNumber.ONE);

		// Then
		assertThat(numberedBookmarks.getBookmark(BookmarkNumber.ONE)).isEmpty();
		assertThat(numberedBookmarks.getBookmarkNumber(bookmarkId)).isEmpty();
		Map<String, String> expectedMap = new HashMap<>();
		expectedMap.put("bookmarkId", null);
		expectedMap.put("bookmarkNumber", "ONE");
		verify(eventBroker).post(BookmarksEvents.TOPIC_NUMBERED_BOOKMARKS_CHANGED, expectedMap);
	}
}
