package mesfavoris.internal.remote;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import mesfavoris.remote.IRemoteBookmarksStore;

public class RemoteBookmarksStoreLoaderTest {

	@Test
	public void testLoadRemoteBookmarksStore() {
		// Given
		RemoteBookmarksStoreLoader loader = new RemoteBookmarksStoreLoader();

		// When
		List<IRemoteBookmarksStore> remoteBookmarksStore = loader.get();

		// Then
		assertThat(remoteBookmarksStore).hasOnlyOneElementSatisfying(
				store -> store.getDescriptor().getId().equals("mesfavoris.remoteStorage.gdrive"));
	}

}
