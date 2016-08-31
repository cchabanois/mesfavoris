package mesfavoris.internal.propertytester;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.IStatus;

import mesfavoris.BookmarksPlugin;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.remote.RemoteBookmarksStoreManager;
import mesfavoris.validation.BookmarkModificationValidator;
import mesfavoris.validation.IBookmarkModificationValidator;

public class BookmarksPropertyTester extends PropertyTester {
	private final BookmarkDatabase bookmarkDatabase;
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;
	private final IBookmarkModificationValidator bookmarkModificationValidator;

	public BookmarksPropertyTester() {
		this(BookmarksPlugin.getBookmarkDatabase(), BookmarksPlugin.getRemoteBookmarksStoreManager());
	}

	public BookmarksPropertyTester(BookmarkDatabase bookmarkDatabase,
			RemoteBookmarksStoreManager remoteBookmarksStoreManager) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.remoteBookmarksStoreManager = remoteBookmarksStoreManager;
		this.bookmarkModificationValidator = new BookmarkModificationValidator(remoteBookmarksStoreManager);
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
		return false;
	}

	private boolean isUnderRemoteBookmarkFolder(Bookmark bookmark) {
		return remoteBookmarksStoreManager.getRemoteBookmarkFolderContaining(bookmarkDatabase.getBookmarksTree(),
				bookmark.getId()) != null;
	}

	private boolean canBeModified(final Bookmark bookmark) {
		IStatus status = bookmarkModificationValidator.validateModification(bookmarkDatabase.getBookmarksTree(),
				bookmark.getId());
		return status.isOK();
	}

}
