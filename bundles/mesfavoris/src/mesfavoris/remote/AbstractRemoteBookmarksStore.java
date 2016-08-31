package mesfavoris.remote;

import org.eclipse.e4.core.services.events.IEventBroker;

import com.google.common.collect.ImmutableMap;

import mesfavoris.model.BookmarkId;

public abstract class AbstractRemoteBookmarksStore implements IRemoteBookmarksStore {
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
		eventBroker.post(TOPIC_MAPPING_ADDED, ImmutableMap.of(PROP_REMOTE_BOOKMARKS_STORE_ID, getDescriptor().getId(),
				PROP_BOOKMARK_FOLDER_ID, bookmarkFolderId.toString()));
	}

	protected void postMappingRemoved(BookmarkId bookmarkFolderId) {
		eventBroker.post(TOPIC_MAPPING_REMOVED, ImmutableMap.of(PROP_REMOTE_BOOKMARKS_STORE_ID, getDescriptor().getId(),
				PROP_BOOKMARK_FOLDER_ID, bookmarkFolderId.toString()));
	}

	protected void postRemoteBookmarksTreeChanged(BookmarkId bookmarkFolderId) {
		eventBroker.post(TOPIC_MAPPING_CHANGED, ImmutableMap.of(PROP_REMOTE_BOOKMARKS_STORE_ID, getDescriptor().getId(),
				PROP_BOOKMARK_FOLDER_ID, bookmarkFolderId.toString()));
	}

	public static String getConnectedTopic(String remoteBookmarksStoreId) {
		return TOPIC_REMOTE_BOOKMARK_STORES + "/" + remoteBookmarksStoreId + "/connected";
	}

}
