package mesfavoris.remote;

import mesfavoris.model.BookmarksTree;

public class RemoteBookmarksTree {
	private final IRemoteBookmarksStore remoteBookmarksStore;
	private final BookmarksTree bookmarksTree;
	private final String etag;
	
	public RemoteBookmarksTree(IRemoteBookmarksStore remoteBookmarksStore, BookmarksTree bookmarksTree, String etag) {
		this.remoteBookmarksStore = remoteBookmarksStore;
		this.bookmarksTree = bookmarksTree;
		this.etag = etag;
	}
	
	public BookmarksTree getBookmarksTree() {
		return bookmarksTree;
	}
	
	public IRemoteBookmarksStore getRemoteBookmarksStore() {
		return remoteBookmarksStore;
	}
	
	public String getEtag() {
		return etag;
	}
	
}