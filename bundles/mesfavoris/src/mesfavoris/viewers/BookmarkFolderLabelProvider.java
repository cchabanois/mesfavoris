package mesfavoris.viewers;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import mesfavoris.BookmarksPlugin;
import mesfavoris.bookmarktype.AbstractBookmarkLabelProvider;
import mesfavoris.internal.views.StylerProvider;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.remote.IRemoteBookmarksStore;
import mesfavoris.remote.IRemoteBookmarksStore.State;
import mesfavoris.remote.RemoteBookmarkFolder;
import mesfavoris.remote.RemoteBookmarksStoreManager;

public class BookmarkFolderLabelProvider extends AbstractBookmarkLabelProvider {
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;
	private final StylerProvider stylerProvider = new StylerProvider();

	public BookmarkFolderLabelProvider() {
		this.remoteBookmarksStoreManager = BookmarksPlugin.getRemoteBookmarksStoreManager();
	}

	@Override
	public StyledString getStyledText(Object element) {
		BookmarkFolder bookmarkFolder = (BookmarkFolder) element;
		StyledString result = super.getStyledText(element);
		RemoteBookmarkFolder remoteBookmarkFolder = remoteBookmarksStoreManager
				.getRemoteBookmarkFolder(bookmarkFolder.getId());
		if (remoteBookmarkFolder != null) {
			IRemoteBookmarksStore remoteBookmarksStore = remoteBookmarksStoreManager
					.getRemoteBookmarksStore(remoteBookmarkFolder.getRemoteBookmarkStoreId());
			if (remoteBookmarksStore.getState() == State.connected && isReadOnly(remoteBookmarkFolder)) {
				result.append(" [readonly]", stylerProvider.getStyler(null,
						Display.getCurrent().getSystemColor(SWT.COLOR_DARK_YELLOW), null));
			}
		}

		return result;
	}

	private boolean isReadOnly(RemoteBookmarkFolder remoteBookmarkFolder) {
		return Boolean.TRUE.toString()
				.equalsIgnoreCase(remoteBookmarkFolder.getProperties().get(RemoteBookmarkFolder.PROP_READONLY));
	}

	@Override
	public Image getImage(Object element) {
		String imageKey = ISharedImages.IMG_OBJ_FOLDER;
		return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
	}

	@Override
	public boolean handlesBookmark(Bookmark bookmark) {
		return bookmark instanceof BookmarkFolder;
	}

}
