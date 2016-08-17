package mesfavoris.internal.persistence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.google.common.collect.Lists;

import mesfavoris.BookmarksException;
import mesfavoris.internal.model.copy.BookmarksCopier;
import mesfavoris.internal.model.replay.ModificationsReplayer;
import mesfavoris.internal.model.utils.BookmarksTreeUtils;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.model.modification.BookmarkDeletedModification;
import mesfavoris.model.modification.BookmarkPropertiesModification;
import mesfavoris.model.modification.BookmarksAddedModification;
import mesfavoris.model.modification.BookmarksModification;
import mesfavoris.model.modification.BookmarksMovedModification;
import mesfavoris.model.modification.BookmarksTreeModifier;
import mesfavoris.remote.ConflictException;
import mesfavoris.remote.IRemoteBookmarksStore;
import mesfavoris.remote.RemoteBookmarkFolder;
import mesfavoris.remote.RemoteBookmarksStoreManager;
import mesfavoris.remote.RemoteBookmarksTree;

/**
 * Apply changes to remote bookmarks
 * 
 * @author cchabanois
 *
 */
public class RemoteBookmarksSaver {
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;

	public RemoteBookmarksSaver(RemoteBookmarksStoreManager remoteBookmarksStoreManager) {
		this.remoteBookmarksStoreManager = remoteBookmarksStoreManager;
	}

	/**
	 * Apply given modifications to remote bookmark stores
	 * 
	 * @param bookmarksModifications
	 * @param monitor
	 * @return true if remote bookmark stores modified
	 * @throws BookmarksException
	 */
	public boolean applyModificationsToRemoteBookmarksStores(List<BookmarksModification> bookmarksModifications,
			IProgressMonitor monitor) throws BookmarksException {
		Map<RemoteBookmarkFolder, List<BookmarksModification>> remoteBookmarkFolders = getRemoteBookmarkFolders(
				bookmarksModifications);
		if (remoteBookmarkFolders.isEmpty()) {
			return false;
		}
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Saving to remote stores", remoteBookmarkFolders.size());
		try {
			for (Map.Entry<RemoteBookmarkFolder, List<BookmarksModification>> entry : remoteBookmarkFolders
					.entrySet()) {
				applyModificationsToRemoteBookmarkFolder(entry.getKey(), entry.getValue(), subMonitor.newChild(1));
			}
			return true;
		} catch (IOException e) {
			throw new BookmarksException("Could not save bookmarks", e);
		}

	}

	/**
	 * 
	 * @param remoteBookmarkFolder
	 * @param modifications
	 * @param monitor
	 * @return true if
	 * @throws IOException
	 */
	private void applyModificationsToRemoteBookmarkFolder(RemoteBookmarkFolder remoteBookmarkFolder,
			List<BookmarksModification> modifications, IProgressMonitor monitor) throws IOException {
		IRemoteBookmarksStore store = remoteBookmarksStoreManager
				.getRemoteBookmarksStore(remoteBookmarkFolder.getRemoteBookmarkStoreId());
		monitor.beginTask("Saving to remote bookmark folder", 100);
		while (true) {
			RemoteBookmarksTree remoteBookmarksTree = store.load(remoteBookmarkFolder.getBookmarkFolderId(),
					new SubProgressMonitor(monitor, 50));
			ModificationsReplayer modificationsReplayer = new ModificationsReplayer(modifications);
			BookmarksTreeModifier remoteBookmarksTreeModifier = new BookmarksTreeModifier(
					remoteBookmarksTree.getBookmarksTree());
			List<BookmarksModification> modificationsNotReplayed = modificationsReplayer
					.replayModifications(remoteBookmarksTreeModifier);
			if (remoteBookmarksTreeModifier.getOriginalTree() == remoteBookmarksTreeModifier.getCurrentTree()) {
				return;
			}
			try {
				store.save(remoteBookmarksTreeModifier.getCurrentTree(),
						remoteBookmarksTreeModifier.getCurrentTree().getRootFolder().getId(),
						remoteBookmarksTree.getEtag(), new SubProgressMonitor(monitor, 50));
				return;
			} catch (ConflictException e) {
				// conflict occurred, reload and retry
			}
		}
	}

	private Map<RemoteBookmarkFolder, List<BookmarksModification>> getRemoteBookmarkFolders(
			List<BookmarksModification> modifications) {
		Map<RemoteBookmarkFolder, List<BookmarksModification>> result = new HashMap<>();
		for (BookmarksModification event : modifications) {
			if (event instanceof BookmarkDeletedModification) {
				BookmarkDeletedModification modification = (BookmarkDeletedModification) event;
				RemoteBookmarkFolder remoteBookmarkFolder = remoteBookmarksStoreManager
						.getRemoteBookmarkFolderContaining(modification.getSourceTree(), modification.getBookmarkId());
				if (remoteBookmarkFolder != null) {
					add(result, remoteBookmarkFolder, modification);
				}
			} else if (event instanceof BookmarksAddedModification) {
				BookmarksAddedModification modification = (BookmarksAddedModification) event;
				RemoteBookmarkFolder remoteBookmarkFolder = remoteBookmarksStoreManager
						.getRemoteBookmarkFolderContaining(modification.getSourceTree(), modification.getParentId());
				if (remoteBookmarkFolder != null) {
					add(result, remoteBookmarkFolder, modification);
				}
			} else if (event instanceof BookmarkPropertiesModification) {
				BookmarkPropertiesModification modification = (BookmarkPropertiesModification) event;
				RemoteBookmarkFolder remoteBookmarkFolder = remoteBookmarksStoreManager
						.getRemoteBookmarkFolderContaining(modification.getSourceTree(), modification.getBookmarkId());
				if (remoteBookmarkFolder != null) {
					add(result, remoteBookmarkFolder, modification);
				}
			} else if (event instanceof BookmarksMovedModification) {
				BookmarksMovedModification modification = (BookmarksMovedModification) event;
				RemoteBookmarkFolder remoteBookmarkFolderSource = getSourceRemoteBookmarkFolder(modification);
				RemoteBookmarkFolder remoteBookmarkFolderTarget = remoteBookmarksStoreManager
						.getRemoteBookmarkFolderContaining(modification.getSourceTree(), modification.getNewParentId());
				if (remoteBookmarkFolderSource != null
						&& remoteBookmarkFolderSource.equals(remoteBookmarkFolderTarget)) {
					add(result, remoteBookmarkFolderSource, modification);
				} else if (remoteBookmarkFolderSource != null || remoteBookmarkFolderTarget != null) {
					getRemoteBookmarkFolders(movedModificationToDeleteAddModifications(modification))
							.forEach((remoteBookmarkFolder, deleteAddModifications) -> add(result, remoteBookmarkFolder,
									deleteAddModifications));
				}
			}
		}
		return result;
	}

	private RemoteBookmarkFolder getSourceRemoteBookmarkFolder(BookmarksMovedModification modification) {
		if (remoteBookmarksStoreManager.getRemoteBookmarkFolder(modification.getBookmarkIds().get(0)) != null) {
			// the remote bookmark folder has been moved
			return null;
		} else {
			return remoteBookmarksStoreManager.getRemoteBookmarkFolderContaining(modification.getSourceTree(),
					modification.getBookmarkIds().get(0));
		}
	}

	private List<BookmarksModification> movedModificationToDeleteAddModifications(
			BookmarksMovedModification modification) {
		BookmarksTreeModifier bookmarksTreeModifier = new BookmarksTreeModifier(modification.getSourceTree());
		for (BookmarkId bookmarkId : modification.getBookmarkIds()) {
			bookmarksTreeModifier.deleteBookmark(bookmarkId, true);
		}
		BookmarksCopier bookmarksCopier = new BookmarksCopier(modification.getSourceTree(), bookmarkId -> bookmarkId);
		Bookmark bookmarkBefore = BookmarksTreeUtils.getBookmarkBefore(modification.getTargetTree(),
				modification.getBookmarkIds().get(0),
				b -> bookmarksTreeModifier.getCurrentTree().getBookmark(b.getId()) != null);
		bookmarksCopier.copyAfter(bookmarksTreeModifier, modification.getNewParentId(),
				bookmarkBefore == null ? null : bookmarkBefore.getId(), modification.getBookmarkIds());
		return bookmarksTreeModifier.getModifications();
	}

	private void add(Map<RemoteBookmarkFolder, List<BookmarksModification>> map,
			RemoteBookmarkFolder remoteBookmarkFolder, BookmarksModification modification) {
		add(map, remoteBookmarkFolder, Lists.newArrayList(modification));
	}

	private void add(Map<RemoteBookmarkFolder, List<BookmarksModification>> map,
			RemoteBookmarkFolder remoteBookmarkFolder, List<BookmarksModification> modifications) {
		List<BookmarksModification> list = map.get(remoteBookmarkFolder);
		if (list == null) {
			list = new ArrayList<>();
			map.put(remoteBookmarkFolder, list);
		}
		list.addAll(modifications);
	}

}
