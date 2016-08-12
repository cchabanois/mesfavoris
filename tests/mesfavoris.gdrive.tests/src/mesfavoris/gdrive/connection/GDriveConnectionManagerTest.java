package mesfavoris.gdrive.connection;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import mesfavoris.gdrive.Constants;
import mesfavoris.gdrive.connection.GDriveConnectionManager;
import mesfavoris.gdrive.test.HtmlUnitAuthorizationCodeInstalledApp;
import mesfavoris.remote.IRemoteBookmarksStore.State;

import static org.mockito.Mockito.*;

public class GDriveConnectionManagerTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private GDriveConnectionManager gDriveConnectionManager;
	private IConnectionListener connectionListener = mock(IConnectionListener.class);

	@Before
	public void setUp() throws Exception {
		File dataStoreDir = temporaryFolder.newFolder();
		String applicationFolderName = "gdriveConnectionManagerTest" + new Random().nextInt(1000);
		gDriveConnectionManager = new GDriveConnectionManager(dataStoreDir,
				new HtmlUnitAuthorizationCodeInstalledApp.Provider(Constants.TEST_USERNAME, Constants.TEST_PASSWORD),
				"mes favoris", applicationFolderName);
		gDriveConnectionManager.init();
		gDriveConnectionManager.addConnectionListener(connectionListener);
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
	}

}
