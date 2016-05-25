package org.chabanois.mesfavoris.remote;

import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.topics.BookmarksEvents;
import org.eclipse.e4.core.services.events.IEventBroker;

public abstract class AbstractRemoteBookmarksStore implements IRemoteBookmarksStore {
	private static final String TOPIC_REMOTE_BOOKMARK_STORES = BookmarksEvents.BOOKMARKS_TOPIC_BASE
			+ "/remoteBookmarkStores";
	public static final String TOPIC_REMOTE_BOOKMARK_STORES_ALL = TOPIC_REMOTE_BOOKMARK_STORES + "/*";
	private final IEventBroker eventBroker;
	private IRemoteBookmarksStoreDescriptor descriptor;

	public AbstractRemoteBookmarksStore(IEventBroker eventBroker) {
		this.eventBroker = eventBroker;
	}

	public void init(IRemoteBookmarksStoreDescriptor descriptor) {
		this.descriptor = descriptor;
	}

	@Override
	public IRemoteBookmarksStoreDescriptor getDescriptor() {
		return descriptor;
	}

	protected void postConnected() {
		eventBroker.post(getConnectedTopic(getDescriptor().getId()), true);
	}

	protected void postDisconnected() {
		eventBroker.post(getConnectedTopic(getDescriptor().getId()), false);
	}

	protected void postMappingAdded(BookmarkId bookmarkFolderId) {
		eventBroker.post(TOPIC_REMOTE_BOOKMARK_STORES + "/" + getDescriptor().getId() + "/mappings/added",
				bookmarkFolderId.toString());
	}

	protected void postMappingRemoved(BookmarkId bookmarkFolderId) {
		eventBroker.post(TOPIC_REMOTE_BOOKMARK_STORES + "/" + getDescriptor().getId() + "/mappings/removed",
				bookmarkFolderId.toString());
	}	
	
	public static String getConnectedTopic(String remoteBookmarksStoreId) {
		return TOPIC_REMOTE_BOOKMARK_STORES + "/" + remoteBookmarksStoreId + "/connected";
	}

}
