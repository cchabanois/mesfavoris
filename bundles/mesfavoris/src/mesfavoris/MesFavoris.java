package mesfavoris;

import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.placeholders.PathPlaceholderResolver;
import mesfavoris.markers.IBookmarksMarkers;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.placeholders.IPathPlaceholderResolver;
import mesfavoris.service.IBookmarksService;

/**
 * Central access point for the mesfavoris plug-in (id
 * <code>"mesfavoris"</code>).
 * <p>
 * This class provides static methods and fields only; it is not intended to be
 * instantiated or subclassed by clients.
 * </p>
 *
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class MesFavoris {

	public static BookmarkDatabase getBookmarkDatabase() {
		if (BookmarksPlugin.getDefault() == null) {
			return null;
		}
		return BookmarksPlugin.getDefault().getBookmarkDatabase();
	}

	public static IBookmarksService getBookmarksService() {
		if (BookmarksPlugin.getDefault() == null) {
			return null;
		}
		return BookmarksPlugin.getDefault().getBookmarksService();
	}

	public static IPathPlaceholderResolver getPathPlaceholderResolver() {
		if (BookmarksPlugin.getDefault() == null) {
			return null;
		}
		return new PathPlaceholderResolver(BookmarksPlugin.getDefault().getPathPlaceholdersStore());
	}
	
	public static IBookmarksMarkers getBookmarksMarkers() {
		if (BookmarksPlugin.getDefault() == null) {
			return null;
		}
		return BookmarksPlugin.getDefault().getBookmarksMarkers();
	}
}
