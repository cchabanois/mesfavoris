package mesfavoris.viewers;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;

import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.remote.IRemoteBookmarksStore;
import mesfavoris.remote.IRemoteBookmarksStoreDescriptor;
import mesfavoris.remote.RemoteBookmarksStoreManager;

public class RemoteBookmarkFolderDecorationProvider implements IBookmarkDecorationProvider {
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;

	public RemoteBookmarkFolderDecorationProvider(RemoteBookmarksStoreManager remoteBookmarksStoreManager) {
		this.remoteBookmarksStoreManager = remoteBookmarksStoreManager;
	}

	@Override
	public ImageDescriptor[] apply(Bookmark bookmark) {
		ImageDescriptor[] overlayImages = new ImageDescriptor[5];
		if (!(bookmark instanceof BookmarkFolder)) {
			return overlayImages;
		}
		IRemoteBookmarksStoreDescriptor storeDescriptor = getRemoteBookmarkStoreDescriptor(bookmark.getId());
		if (storeDescriptor != null) {
			overlayImages[IDecoration.TOP_RIGHT] = storeDescriptor.getImageOverlayDescriptor();
		}
		return overlayImages;
	}

	private IRemoteBookmarksStoreDescriptor getRemoteBookmarkStoreDescriptor(BookmarkId bookmarkFolderId) {
		for (IRemoteBookmarksStore store : remoteBookmarksStoreManager.getRemoteBookmarksStores()) {
			if (store.getRemoteBookmarkFolderIds().contains(bookmarkFolderId)) {
				return store.getDescriptor();
			}
		}
		return null;
	}

}
