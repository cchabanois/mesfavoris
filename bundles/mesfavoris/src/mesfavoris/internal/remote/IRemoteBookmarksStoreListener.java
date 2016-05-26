package mesfavoris.internal.remote;

import mesfavoris.remote.IRemoteBookmarksStore;

public interface IRemoteBookmarksStoreListener {

	void connected(IRemoteBookmarksStore remoteBookmarksStore);

	void disconnected(IRemoteBookmarksStore remoteBookmarksStore);

}
