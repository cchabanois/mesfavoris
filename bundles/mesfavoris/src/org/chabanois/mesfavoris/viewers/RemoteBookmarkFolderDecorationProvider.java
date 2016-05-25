package org.chabanois.mesfavoris.viewers;

import java.util.function.Function;

import org.chabanois.mesfavoris.model.Bookmark;
import org.chabanois.mesfavoris.model.BookmarkFolder;
import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.remote.IRemoteBookmarksStore;
import org.chabanois.mesfavoris.remote.IRemoteBookmarksStoreDescriptor;
import org.chabanois.mesfavoris.remote.RemoteBookmarksStoreManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;

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
