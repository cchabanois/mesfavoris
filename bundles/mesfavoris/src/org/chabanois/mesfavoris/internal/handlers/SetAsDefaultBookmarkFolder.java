package org.chabanois.mesfavoris.internal.handlers;

import org.chabanois.mesfavoris.BookmarksException;
import org.chabanois.mesfavoris.BookmarksPlugin;
import org.chabanois.mesfavoris.model.Bookmark;
import org.chabanois.mesfavoris.model.BookmarkDatabase;
import org.chabanois.mesfavoris.model.BookmarkFolder;
import org.chabanois.mesfavoris.remote.IRemoteBookmarksStore;
import org.chabanois.mesfavoris.remote.IRemoteBookmarksStore.State;
import org.chabanois.mesfavoris.remote.RemoteBookmarkFolder;
import org.chabanois.mesfavoris.remote.RemoteBookmarksStoreManager;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

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
