package mesfavoris.remote;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.topics.BookmarksEvents;

public interface IRemoteBookmarksStore {

	public static final String PROP_BOOKMARK_FOLDER_ID = "bookmarkFolderId";
	public static final String PROP_REMOTE_BOOKMARKS_STORE_ID = "remoteBookmarksStoreId";
	public static final String TOPIC_REMOTE_BOOKMARK_STORES = BookmarksEvents.BOOKMARKS_TOPIC_BASE
			+ "/remoteBookmarkStores";
	public static final String TOPIC_MAPPING_ADDED = TOPIC_REMOTE_BOOKMARK_STORES + "/mappings/added";
	public static final String TOPIC_MAPPING_REMOVED = TOPIC_REMOTE_BOOKMARK_STORES + "/mappings/removed";
	public static final String TOPIC_MAPPING_CHANGED = TOPIC_REMOTE_BOOKMARK_STORES + "/mappings/changed";
	public static final String TOPIC_REMOTE_BOOKMARK_STORES_ALL = TOPIC_REMOTE_BOOKMARK_STORES + "/*";
	
	public static enum State {
		disconnected, connecting, connected
	}

	public IRemoteBookmarksStoreDescriptor getDescriptor();
	
	public void connect(IProgressMonitor monitor) throws IOException;

	public void disconnect(IProgressMonitor monitor) throws IOException;

	public State getState();

	public RemoteBookmarksTree add(BookmarksTree bookmarksTree, BookmarkId bookmarkFolderId,
			IProgressMonitor monitor) throws IOException;

	public void remove(BookmarkId bookmarkFolderId, IProgressMonitor monitor)
			throws IOException;

	public Set<RemoteBookmarkFolder> getRemoteBookmarkFolders();

	public Optional<RemoteBookmarkFolder> getRemoteBookmarkFolder(BookmarkId bookmarkFolderId);
	
	public RemoteBookmarksTree load(BookmarkId bookmarkFolderId,
			IProgressMonitor monitor) throws IOException;

	public RemoteBookmarksTree save(BookmarksTree bookmarksTree, BookmarkId bookmarkFolderId, String etag,
			IProgressMonitor monitor) throws IOException, ConflictException;

}
