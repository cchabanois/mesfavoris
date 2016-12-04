package mesfavoris.internal.propertytester;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.IStatus;

import mesfavoris.BookmarksPlugin;
import mesfavoris.internal.workspace.DefaultBookmarkFolderProvider;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.remote.RemoteBookmarksStoreManager;

public class BookmarksPropertyTester extends PropertyTester {
	private final BookmarkDatabase bookmarkDatabase;
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;
	private final DefaultBookmarkFolderProvider defaultBookmarkFolderProvider;

	public BookmarksPropertyTester() {
		this(BookmarksPlugin.getBookmarkDatabase(), BookmarksPlugin.getRemoteBookmarksStoreManager(),
				BookmarksPlugin.getDefaultBookmarkFolderProvider());
	}

	public BookmarksPropertyTester(BookmarkDatabase bookmarkDatabase,
			RemoteBookmarksStoreManager remoteBookmarksStoreManager,
			DefaultBookmarkFolderProvider defaultBookmarkFolderProvider) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.remoteBookmarksStoreManager = remoteBookmarksStoreManager;
		this.defaultBookmarkFolderProvider = defaultBookmarkFolderProvider;
	}

	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (!(receiver instanceof Bookmark)) {
			return false;
		}
		Bookmark bookmark = (Bookmark) receiver;
		if ("canBeModified".equals(property)) {
			return canBeModified(bookmark);
		}
		if ("isUnderRemoteBookmarkFolder".equals(property)) {
			return isUnderRemoteBookmarkFolder(bookmark);
		}
		if ("isDefaultBookmarkFolder".equals(property)) {
			return isDefaultBookmarkFolder(bookmark);
		}
		return false;
	}

	private boolean isDefaultBookmarkFolder(Bookmark bookmark) {
		return defaultBookmarkFolderProvider.getDefaultBookmarkFolder().equals(bookmark.getId());
	}

	private boolean isUnderRemoteBookmarkFolder(Bookmark bookmark) {
		return remoteBookmarksStoreManager.getRemoteBookmarkFolderContaining(bookmarkDatabase.getBookmarksTree(),
				bookmark.getId()) != null;
	}

	private boolean canBeModified(final Bookmark bookmark) {
		IStatus status = bookmarkDatabase.getBookmarksModificationValidator()
				.validateModification(bookmarkDatabase.getBookmarksTree(), bookmark.getId());
		return status.isOK();
	}

}
