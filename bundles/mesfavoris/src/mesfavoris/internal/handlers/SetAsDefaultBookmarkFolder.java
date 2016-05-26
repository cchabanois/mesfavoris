package mesfavoris.internal.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import mesfavoris.BookmarksException;
import mesfavoris.BookmarksPlugin;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.remote.IRemoteBookmarksStore;
import mesfavoris.remote.RemoteBookmarkFolder;
import mesfavoris.remote.RemoteBookmarksStoreManager;
import mesfavoris.remote.IRemoteBookmarksStore.State;

public class SetAsDefaultBookmarkFolder extends AbstractBookmarkHandler {
	private final BookmarkDatabase bookmarkDatabase;
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;

	public SetAsDefaultBookmarkFolder() {
		this.bookmarkDatabase = BookmarksPlugin.getBookmarkDatabase();
		this.remoteBookmarksStoreManager = BookmarksPlugin.getRemoteBookmarksStoreManager();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		BookmarkFolder bookmarkFolder = getSelectedBookmarkFolder(selection);
		if (bookmarkFolder == null) {
			return null;
		}
		try {
			setAsDefaultBookmarkFolder(bookmarkFolder);
		} catch (BookmarksException e) {
			throw new ExecutionException("Could not set as default bookmark folder", e);
		}
		return null;
	}

	@Override
	public boolean isEnabled() {
		BookmarkFolder bookmarkFolder = getSelectedBookmarkFolder(getSelection());
		RemoteBookmarkFolder remoteBookmarkFolder = remoteBookmarksStoreManager
				.getRemoteBookmarkFolderContaining(bookmarkDatabase.getBookmarksTree(), bookmarkFolder.getId());
		if (remoteBookmarkFolder == null) {
			return true;
		}
		IRemoteBookmarksStore remoteBookmarksStore = remoteBookmarksStoreManager
				.getRemoteBookmarksStore(remoteBookmarkFolder.getRemoteBookmarkStoreId());
		return remoteBookmarksStore.getState() == State.connected;
	}

	private BookmarkFolder getSelectedBookmarkFolder(IStructuredSelection selection) {
		if (selection.isEmpty()) {
			return null;
		}
		Bookmark bookmark = (Bookmark) selection.getFirstElement();
		if (!(bookmark instanceof BookmarkFolder)) {
			return null;
		}
		BookmarkFolder bookmarkFolder = (BookmarkFolder) bookmark;
		return bookmarkFolder;
	}

	private void setAsDefaultBookmarkFolder(final BookmarkFolder bookmarkFolder) throws BookmarksException {
		BookmarksPlugin.getDefaultBookmarkFolderManager().setDefaultFolder(bookmarkFolder);
	}

}
