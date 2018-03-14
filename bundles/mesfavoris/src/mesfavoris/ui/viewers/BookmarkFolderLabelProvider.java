package mesfavoris.ui.viewers;

import java.util.Optional;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import mesfavoris.bookmarktype.AbstractBookmarkLabelProvider;
import mesfavoris.commons.ui.viewers.StylerProvider;
import mesfavoris.internal.BookmarksPlugin;
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
		this.remoteBookmarksStoreManager = BookmarksPlugin.getDefault().getRemoteBookmarksStoreManager();
	}

	@Override
	public StyledString getStyledText(Context context, Bookmark bookmark) {
		BookmarkFolder bookmarkFolder = (BookmarkFolder) bookmark;
		StyledString result = super.getStyledText(context, bookmark);

		Optional<RemoteBookmarkFolder> remoteBookmarkFolder = remoteBookmarksStoreManager
				.getRemoteBookmarkFolder(bookmarkFolder.getId());
		remoteBookmarkFolder.map(f -> getBookmarksCount(f)).ifPresent(bookmarksCount -> {
			result.append(String.format(" (%d)", bookmarksCount),
					stylerProvider.getStyler(null, Display.getCurrent().getSystemColor(SWT.COLOR_DARK_YELLOW), null));
		});
		Optional<IRemoteBookmarksStore> remoteBookmarksStore = remoteBookmarkFolder
				.flatMap(f -> remoteBookmarksStoreManager.getRemoteBookmarksStore(f.getRemoteBookmarkStoreId()));
		if (remoteBookmarksStore.filter(store -> store.getState() == State.connected).isPresent()
				&& remoteBookmarkFolder.filter(f -> isReadOnly(f)).isPresent()) {
			result.append(" [readonly]",
					stylerProvider.getStyler(null, Display.getCurrent().getSystemColor(SWT.COLOR_DARK_YELLOW), null));
		}

		return result;
	}

	private boolean isReadOnly(RemoteBookmarkFolder remoteBookmarkFolder) {
		return Boolean.TRUE.toString()
				.equalsIgnoreCase(remoteBookmarkFolder.getProperties().get(RemoteBookmarkFolder.PROP_READONLY));
	}

	private Optional<Integer> getBookmarksCount(RemoteBookmarkFolder remoteBookmarkFolder) {
		String bookmarksCount = remoteBookmarkFolder.getProperties().get(RemoteBookmarkFolder.PROP_BOOKMARKS_COUNT);
		if (bookmarksCount == null) {
			return Optional.empty();
		}
		try {
			return Optional.of(Integer.parseInt(bookmarksCount));
		} catch (NumberFormatException e) {
			return Optional.empty();
		}
	}

	@Override
	public ImageDescriptor getImageDescriptor(Context context, Bookmark bookmark) {
		String imageKey = ISharedImages.IMG_OBJ_FOLDER;
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(imageKey);
	}

	@Override
	public boolean canHandle(Context context, Bookmark bookmark) {
		return bookmark instanceof BookmarkFolder;
	}

}
