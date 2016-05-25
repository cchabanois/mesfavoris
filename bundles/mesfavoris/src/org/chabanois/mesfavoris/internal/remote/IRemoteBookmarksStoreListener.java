package org.chabanois.mesfavoris.internal.remote;

import org.chabanois.mesfavoris.remote.IRemoteBookmarksStore;

public interface IRemoteBookmarksStoreListener {

	void connected(IRemoteBookmarksStore remoteBookmarksStore);

	void disconnected(IRemoteBookmarksStore remoteBookmarksStore);

}
