package mesfavoris.remote;

import java.io.IOException;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;

public interface IRemoteBookmarksStore {

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

	public Set<BookmarkId> getRemoteBookmarkFolderIds();

	public RemoteBookmarksTree load(BookmarkId bookmarkFolderId,
			IProgressMonitor monitor) throws IOException;

	public RemoteBookmarksTree save(BookmarksTree bookmarksTree, BookmarkId bookmarkFolderId, String etag,
			IProgressMonitor monitor) throws IOException, ConflictException;

}
