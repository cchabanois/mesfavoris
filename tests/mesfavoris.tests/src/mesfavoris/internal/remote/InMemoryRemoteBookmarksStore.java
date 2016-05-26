package mesfavoris.internal.remote;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;

import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.remote.AbstractRemoteBookmarksStore;
import mesfavoris.remote.ConflictException;
import mesfavoris.remote.IRemoteBookmarksStoreDescriptor;
import mesfavoris.remote.RemoteBookmarksTree;

public class InMemoryRemoteBookmarksStore extends AbstractRemoteBookmarksStore {
	private AtomicReference<State> state = new AtomicReference<>(State.disconnected);
	private ConcurrentMap<BookmarkId, InMemoryRemoteBookmarksTree> inMemoryRemoteBookmarksTrees = new ConcurrentHashMap<>();

	public InMemoryRemoteBookmarksStore() {
		this((IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class));
	}	
	
	public InMemoryRemoteBookmarksStore(IEventBroker eventBroker) {
		super(eventBroker);
		init(new InMemoryRemoteBookmarksStoreDescriptor());
	}	
	
	@Override
	public void connect(IProgressMonitor monitor) throws IOException {
		state.set(State.connected);
		postConnected();
	}

	@Override
	public void disconnect(IProgressMonitor monitor) throws IOException {
		state.set(State.disconnected);
		postDisconnected();
	}

	@Override
	public State getState() {
		return state.get();
	}

	@Override
	public RemoteBookmarksTree add(BookmarksTree bookmarksTree, BookmarkId bookmarkFolderId, IProgressMonitor monitor)
			throws IOException {
		String etag = UUID.randomUUID().toString();
		BookmarksTree subTree = bookmarksTree.subTree(bookmarkFolderId);
		inMemoryRemoteBookmarksTrees.put(bookmarkFolderId, new InMemoryRemoteBookmarksTree(subTree, etag));
		return new RemoteBookmarksTree(this, subTree, etag);
	}

	@Override
	public void remove(BookmarkId bookmarkFolderId, IProgressMonitor monitor) throws IOException {
		inMemoryRemoteBookmarksTrees.remove(bookmarkFolderId);
	}

	@Override
	public Set<BookmarkId> getRemoteBookmarkFolderIds() {
		return new HashSet<BookmarkId>(inMemoryRemoteBookmarksTrees.keySet());
	}

	@Override
	public RemoteBookmarksTree load(BookmarkId bookmarkFolderId, IProgressMonitor monitor) throws IOException {
		InMemoryRemoteBookmarksTree inMemoryRemoteBookmarksTree = inMemoryRemoteBookmarksTrees.get(bookmarkFolderId);
		if (inMemoryRemoteBookmarksTree == null) {
			throw new IllegalArgumentException();
		}
		return new RemoteBookmarksTree(this, inMemoryRemoteBookmarksTree.bookmarksTree,
				inMemoryRemoteBookmarksTree.etag);
	}

	@Override
	public RemoteBookmarksTree save(BookmarksTree bookmarksTree, BookmarkId bookmarkFolderId, String etag,
			IProgressMonitor monitor) throws IOException, ConflictException {
		InMemoryRemoteBookmarksTree inMemoryRemoteBookmarksTree = inMemoryRemoteBookmarksTrees.get(bookmarkFolderId);
		if (inMemoryRemoteBookmarksTree == null) {
			throw new IllegalArgumentException();
		}
		if (inMemoryRemoteBookmarksTree.etag != etag) {
			throw new ConflictException();
		}
		etag = UUID.randomUUID().toString();
		BookmarksTree subTree = bookmarksTree.subTree(bookmarkFolderId);
		InMemoryRemoteBookmarksTree newInMemoryRemoteBookmarksTree = new InMemoryRemoteBookmarksTree(subTree, etag);
		if (!inMemoryRemoteBookmarksTrees.replace(bookmarkFolderId, inMemoryRemoteBookmarksTree,
				newInMemoryRemoteBookmarksTree)) {
			throw new ConflictException();
		}
		return new RemoteBookmarksTree(this, subTree, etag);
	}

	private static class InMemoryRemoteBookmarksTree {
		private final BookmarksTree bookmarksTree;
		private final String etag;

		public InMemoryRemoteBookmarksTree(BookmarksTree bookmarksTree, String etag) {
			this.bookmarksTree = bookmarksTree;
			this.etag = etag;
		}

	}

	private static class InMemoryRemoteBookmarksStoreDescriptor implements IRemoteBookmarksStoreDescriptor {

		@Override
		public String getId() {
			return "inMemory";
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return null;
		}

		@Override
		public ImageDescriptor getImageOverlayDescriptor() {
			return null;
		}

		@Override
		public String getLabel() {
			return "In Memory";
		}
		
	}
	
}
