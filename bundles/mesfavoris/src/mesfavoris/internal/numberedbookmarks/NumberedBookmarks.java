package mesfavoris.internal.numberedbookmarks;

import java.util.Optional;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.e4.core.services.events.IEventBroker;

import com.google.common.collect.ImmutableMap;

import mesfavoris.model.BookmarkId;
import mesfavoris.topics.BookmarksEvents;

public class NumberedBookmarks {
	private static final String KEY_PREFIX = "bookmark_";
	private final IEclipsePreferences preferences;
	private final IPreferenceChangeListener preferenceChangeListener;

	public NumberedBookmarks(IEclipsePreferences preferences, IEventBroker eventBroker) {
		this.preferences = preferences;
		this.preferenceChangeListener = event -> {
			Optional<BookmarkNumber> bookmarkNumber = getBookmarkNumber(event.getKey());
			if (!bookmarkNumber.isPresent()) {
				return;
			}
			String newValue = (String) event.getNewValue();
			eventBroker.post(BookmarksEvents.TOPIC_NUMBERED_BOOKMARKS_CHANGED,
					ImmutableMap.of("bookmarkId", newValue, "bookmarkNumber", bookmarkNumber.get().toString()));
		};
	}

	public void init() {
		preferences.addPreferenceChangeListener(preferenceChangeListener);
	}

	public void close() {
		preferences.removePreferenceChangeListener(preferenceChangeListener);
	}

	public void set(BookmarkNumber bookmarkNumber, BookmarkId bookmarkId) {
		preferences.put(getKey(bookmarkNumber), bookmarkId.toString());
	}

	public void remove(BookmarkNumber bookmarkNumber) {
		preferences.remove(getKey(bookmarkNumber));
	}

	private String getKey(BookmarkNumber bookmarkNumber) {
		return KEY_PREFIX + bookmarkNumber.toString();
	}

	private Optional<BookmarkNumber> getBookmarkNumber(String key) {
		if (!key.startsWith(KEY_PREFIX)) {
			return Optional.empty();
		}
		try {
			return Optional.of(BookmarkNumber.valueOf(key.substring(KEY_PREFIX.length())));
		} catch (IllegalArgumentException e) {
			return Optional.empty();
		}
	}

	public Optional<BookmarkId> getBookmark(BookmarkNumber bookmarkNumber) {
		String bookmarkIdAsString = preferences.get(getKey(bookmarkNumber), null);
		if (bookmarkIdAsString == null) {
			return Optional.empty();
		}
		return Optional.of(new BookmarkId(bookmarkIdAsString));
	}

	public Optional<BookmarkNumber> getBookmarkNumber(BookmarkId bookmarkId) {
		for (BookmarkNumber bookmarkNumber : BookmarkNumber.values()) {
			Optional<BookmarkId> currentBookmarkId = getBookmark(bookmarkNumber);
			if (currentBookmarkId.isPresent() && currentBookmarkId.get().equals(bookmarkId)) {
				return Optional.of(bookmarkNumber);
			}
		}
		return Optional.empty();
	}

}
