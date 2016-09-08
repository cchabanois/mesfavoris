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
		IRemoteBookmarksStore store = getRemoteBookmarkStore(bookmark.getId());
		if (store == null) {
			return overlayImages;
		}
		IRemoteBookmarksStoreDescriptor storeDescriptor = store.getDescriptor();
		if (storeDescriptor != null) {
			overlayImages[IDecoration.TOP_RIGHT] = storeDescriptor.getImageOverlayDescriptor();
		}
		return overlayImages;
	}

	private IRemoteBookmarksStore getRemoteBookmarkStore(BookmarkId bookmarkFolderId) {
		for (IRemoteBookmarksStore store : remoteBookmarksStoreManager.getRemoteBookmarksStores()) {
			if (store.getRemoteBookmarkFolder(bookmarkFolderId).isPresent()) {
				return store;
			}
		}
		return null;
	}

}
