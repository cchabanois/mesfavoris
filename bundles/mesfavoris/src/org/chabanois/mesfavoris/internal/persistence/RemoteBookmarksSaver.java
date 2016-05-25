package org.chabanois.mesfavoris.internal.persistence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chabanois.mesfavoris.BookmarksException;
import org.chabanois.mesfavoris.internal.model.copy.BookmarksCopier;
import org.chabanois.mesfavoris.internal.model.replay.ModificationsReplayer;
import org.chabanois.mesfavoris.internal.model.utils.BookmarksTreeUtils;
import org.chabanois.mesfavoris.model.Bookmark;
import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.model.modification.BookmarkDeletedModification;
import org.chabanois.mesfavoris.model.modification.BookmarkPropertiesModification;
import org.chabanois.mesfavoris.model.modification.BookmarksAddedModification;
import org.chabanois.mesfavoris.model.modification.BookmarksModification;
import org.chabanois.mesfavoris.model.modification.BookmarksMovedModification;
import org.chabanois.mesfavoris.model.modification.BookmarksTreeModifier;
import org.chabanois.mesfavoris.remote.ConflictException;
import org.chabanois.mesfavoris.remote.IRemoteBookmarksStore;
import org.chabanois.mesfavoris.remote.RemoteBookmarkFolder;
import org.chabanois.mesfavoris.remote.RemoteBookmarksStoreManager;
import org.chabanois.mesfavoris.remote.RemoteBookmarksTree;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.google.common.collect.Lists;

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
	 * @throws BookmarksException
	 */
	public void applyModificationsToRemoteBookmarksStores(List<BookmarksModification> bookmarksModifications,
			IProgressMonitor monitor) throws BookmarksException {
		Map<RemoteBookmarkFolder, List<BookmarksModification>> remoteBookmarkFolders = getRemoteBookmarkFolders(
				bookmarksModifications);
		monitor.beginTask("Saving to remote stores", remoteBookmarkFolders.size());
		try {
			for (Map.Entry<RemoteBookmarkFolder, List<BookmarksModification>> entry : remoteBookmarkFolders
					.entrySet()) {
				applyModificationsToRemoteBookmarkFolder(entry.getKey(), entry.getValue(),
						new SubProgressMonitor(monitor, 1));
			}
		} catch (IOException e) {
			throw new BookmarksException("Could not save bookmarks", e);
		}

	}

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
				RemoteBookmarkFolder remoteBookmarkFolderSource = remoteBookmarksStoreManager
						.getRemoteBookmarkFolderContaining(modification.getSourceTree(),
								modification.getBookmarkIds().get(0));
				RemoteBookmarkFolder remoteBookmarkFolderTarget = remoteBookmarksStoreManager
						.getRemoteBookmarkFolderContaining(modification.getSourceTree(), modification.getNewParentId());
				if (remoteBookmarkFolderSource != null
						&& remoteBookmarkFolderSource.equals(remoteBookmarkFolderTarget)) {
					add(result, remoteBookmarkFolderSource, modification);
				} else {
					getRemoteBookmarkFolders(movedModificationToDeleteAddModifications(modification))
							.forEach((remoteBookmarkFolder, deleteAddModifications) -> add(result,
									remoteBookmarkFolder, deleteAddModifications));
				}
			}
		}
		return result;
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
		bookmarksCopier.copyAfter(bookmarksTreeModifier, modification.getNewParentId(), bookmarkBefore == null ? null : bookmarkBefore.getId(),modification.getBookmarkIds());			
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
