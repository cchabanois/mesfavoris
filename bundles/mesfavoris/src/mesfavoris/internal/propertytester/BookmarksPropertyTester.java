package mesfavoris.internal.propertytester;

import static mesfavoris.internal.Constants.DEFAULT_BOOKMARKFOLDER_ID;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.IStatus;

import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.remote.RemoteBookmarksStoreManager;

public class BookmarksPropertyTester extends PropertyTester {
	public static final String PROPERTY_HAS_PROPERTIES = "hasProperties";
	public static final String PROPERTY_IS_REMOTE_BOOKMARK_FOLDER = "isRemoteBookmarkFolder";
	public static final String PROPERTY_IS_DEFAULT_BOOKMARK_FOLDER = "isDefaultBookmarkFolder";
	public static final String PROPERTY_IS_UNDER_REMOTE_BOOKMARK_FOLDER = "isUnderRemoteBookmarkFolder";
	public static final String PROPERTY_CAN_BE_MODIFIED = "canBeModified";
	private final BookmarkDatabase bookmarkDatabase;
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;

	public BookmarksPropertyTester() {
		this(BookmarksPlugin.getDefault().getBookmarkDatabase(),
				BookmarksPlugin.getDefault().getRemoteBookmarksStoreManager());
	}

	public BookmarksPropertyTester(BookmarkDatabase bookmarkDatabase,
			RemoteBookmarksStoreManager remoteBookmarksStoreManager) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.remoteBookmarksStoreManager = remoteBookmarksStoreManager;
	}

	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (!(receiver instanceof Bookmark)) {
			return false;
		}
		Bookmark bookmark = (Bookmark) receiver;
		if (PROPERTY_CAN_BE_MODIFIED.equals(property)) {
			return canBeModified(bookmark);
		}
		if (PROPERTY_IS_UNDER_REMOTE_BOOKMARK_FOLDER.equals(property)) {
			return isUnderRemoteBookmarkFolder(bookmark);
		}
		if (PROPERTY_IS_DEFAULT_BOOKMARK_FOLDER.equals(property)) {
			return isDefaultBookmarkFolder(bookmark);
		}
		if (PROPERTY_IS_REMOTE_BOOKMARK_FOLDER.equals(property)) {
			return isRemoteBookmarkFolder(bookmark);
		}
		if (PROPERTY_HAS_PROPERTIES.equals(property)) {
			return hasProperties(bookmark, args);
		}
		return false;
	}

	private boolean hasProperties(Bookmark bookmark, Object[] args) {
		for (Object arg : args) {
			if (bookmark.getPropertyValue(arg.toString()) == null) {
				return false;
			}
		}
		return true;
	}

	private boolean isRemoteBookmarkFolder(Bookmark bookmark) {
		return remoteBookmarksStoreManager.getRemoteBookmarkFolder(bookmark.getId()).isPresent();
	}

	private boolean isDefaultBookmarkFolder(Bookmark bookmark) {
		return DEFAULT_BOOKMARKFOLDER_ID.equals(bookmark.getId());
	}

	private boolean isUnderRemoteBookmarkFolder(Bookmark bookmark) {
		return remoteBookmarksStoreManager.getRemoteBookmarkFolderContaining(bookmarkDatabase.getBookmarksTree(),
				bookmark.getId()).isPresent();
	}

	private boolean canBeModified(final Bookmark bookmark) {
		IStatus status = bookmarkDatabase.getBookmarksModificationValidator()
				.validateModification(bookmarkDatabase.getBookmarksTree(), bookmark.getId());
		return status.isOK();
	}

}
