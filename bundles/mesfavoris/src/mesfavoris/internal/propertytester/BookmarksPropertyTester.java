package mesfavoris.internal.propertytester;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.IStatus;

import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.workspace.DefaultBookmarkFolderProvider;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.remote.RemoteBookmarksStoreManager;

public class BookmarksPropertyTester extends PropertyTester {
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
		return DefaultBookmarkFolderProvider.DEFAULT_BOOKMARKFOLDER_ID.equals(bookmark.getId());
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
