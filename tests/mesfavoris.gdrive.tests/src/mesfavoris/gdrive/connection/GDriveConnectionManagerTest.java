package mesfavoris.gdrive.connection;

import static com.google.api.client.auth.oauth2.StoredCredential.DEFAULT_DATA_STORE_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;

import mesfavoris.gdrive.GDriveTestUser;
import mesfavoris.gdrive.test.HtmlUnitAuthorizationCodeInstalledApp;
import mesfavoris.remote.IRemoteBookmarksStore.State;

public class GDriveConnectionManagerTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private GDriveConnectionManager gDriveConnectionManager;
	private IConnectionListener connectionListener = mock(IConnectionListener.class);
	private GDriveTestUser user = GDriveTestUser.USER1;

	@Before
	public void setUp() throws Exception {
		File dataStoreDir = temporaryFolder.newFolder();
		if (user.getCredential().isPresent()) {
			addCredentialToDataStore(dataStoreDir, user.getCredential().get());
		}
		String applicationFolderName = "gdriveConnectionManagerTest" + new Random().nextInt(1000);
		gDriveConnectionManager = new GDriveConnectionManager(dataStoreDir, "mes favoris", applicationFolderName);
		gDriveConnectionManager.init();
		gDriveConnectionManager.addConnectionListener(connectionListener);
	}

	private void addCredentialToDataStore(File dataStoreDir, StoredCredential credential) throws IOException {
		FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(dataStoreDir);
		DataStore<StoredCredential> dataStore = dataStoreFactory.getDataStore(DEFAULT_DATA_STORE_ID);
		dataStore.set("user", credential);
	}

	@After
	public void tearDown() throws IOException {
		deleteApplicationFolder();
		gDriveConnectionManager.removeConnectionListener(connectionListener);
		gDriveConnectionManager.close();
	}

	private void deleteApplicationFolder() throws IOException {
		gDriveConnectionManager.connect(new NullProgressMonitor());
		gDriveConnectionManager.getDrive().files().delete(gDriveConnectionManager.getApplicationFolderId());
	}

	@Test
	public void testConnect() throws Exception {
		// Given

		// When
		gDriveConnectionManager.connect(new NullProgressMonitor());

		// Then
		assertEquals(State.connected, gDriveConnectionManager.getState());
		verify(connectionListener).connected();
		assertNotNull(gDriveConnectionManager.getApplicationFolderId());
		assertEquals(user.getEmail(), gDriveConnectionManager.getUserInfo().getEmailAddress());
		assertNotNull(gDriveConnectionManager.getUserInfo().getDisplayName());
	}

	@Test
	public void testDisconnect() throws Exception {
		// Given
		gDriveConnectionManager.connect(new NullProgressMonitor());

		// When
		gDriveConnectionManager.disconnect(new NullProgressMonitor());

		// Then
		assertEquals(State.disconnected, gDriveConnectionManager.getState());
		verify(connectionListener).disconnected();
		assertNotNull(gDriveConnectionManager.getUserInfo());
	}

}
