package org.chabanois.mesfavoris.viewers;

import org.chabanois.mesfavoris.model.Bookmark;
import org.chabanois.mesfavoris.model.BookmarkDatabase;
import org.chabanois.mesfavoris.model.BookmarkFolder;
import org.chabanois.mesfavoris.remote.IRemoteBookmarksStore;
import org.chabanois.mesfavoris.remote.RemoteBookmarkFolder;
import org.chabanois.mesfavoris.remote.RemoteBookmarksStoreManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;

public class UnderRemoteBookmarkFolderDecorationProvider implements IBookmarkDecorationProvider {
	private final BookmarkDatabase bookmarkDatabase;
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;

	public UnderRemoteBookmarkFolderDecorationProvider(BookmarkDatabase bookmarkDatabase,
			RemoteBookmarksStoreManager remoteBookmarksStoreManager) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.remoteBookmarksStoreManager = remoteBookmarksStoreManager;
	}

	@Override
	public ImageDescriptor[] apply(Bookmark bookmark) {
		ImageDescriptor[] overlayImages = new ImageDescriptor[5];
		RemoteBookmarkFolder remoteBookmarkFolder = remoteBookmarksStoreManager
				.getRemoteBookmarkFolderContaining(bookmarkDatabase.getBookmarksTree(), bookmark.getId());
		if (remoteBookmarkFolder != null) {
			IRemoteBookmarksStore remoteBookmarksStore = remoteBookmarksStoreManager
					.getRemoteBookmarksStore(remoteBookmarkFolder.getRemoteBookmarkStoreId());
			overlayImages[IDecoration.TOP_RIGHT] = remoteBookmarksStore.getDescriptor().getImageOverlayDescriptor();
		}
		return overlayImages;
	}

}
