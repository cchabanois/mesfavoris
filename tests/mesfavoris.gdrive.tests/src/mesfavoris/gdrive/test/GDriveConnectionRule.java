package mesfavoris.gdrive.test;

import static com.google.api.client.auth.oauth2.StoredCredential.DEFAULT_DATA_STORE_ID;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;

import mesfavoris.gdrive.GDriveTestUser;
import mesfavoris.gdrive.connection.GDriveConnectionManager;
import mesfavoris.gdrive.connection.auth.AuthorizationCodeEclipseApp;
import mesfavoris.gdrive.connection.auth.IAuthorizationCodeInstalledAppProvider;

public class GDriveConnectionRule extends ExternalResource {

	private GDriveConnectionManager gDriveConnectionManager;
	private final TemporaryFolder temporaryFolder = new TemporaryFolder();
	private final boolean connect;
	private final IAuthorizationCodeInstalledAppProvider authorizationCodeProvider;
	private final GDriveTestUser user;
	
	public GDriveConnectionRule(GDriveTestUser user, boolean connect) {
		this.user = user;
		this.connect = connect;
		// this does not work anymore. This means we cannot use username/password to get a new refresh token when it expires  
//		this.authorizationCodeProvider = new HtmlUnitAuthorizationCodeInstalledApp.Provider(user.getUserName(),
//				user.getPassword(), user.getRecoveryEmail());
		this.authorizationCodeProvider = new AuthorizationCodeEclipseApp.Provider();
	}

	@Override
	protected void before() throws Throwable {
		temporaryFolder.create();
		java.io.File dataStoreDir = temporaryFolder.newFolder();
		
		if (user.getCredential().isPresent()) {
			addCredentialToDataStore(dataStoreDir, user.getCredential().get());
		}
		
		String applicationFolderName = "gdriveConnectionManagerTest" + new Random().nextInt(1000);
		gDriveConnectionManager = new GDriveConnectionManager(dataStoreDir, authorizationCodeProvider, "mes favoris",
				applicationFolderName);
		gDriveConnectionManager.init();
		if (connect) {
			connect();
		}
	}

	private void addCredentialToDataStore(File dataStoreDir, StoredCredential credential) throws IOException {
		FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(dataStoreDir);
		DataStore<StoredCredential> dataStore = dataStoreFactory.getDataStore(DEFAULT_DATA_STORE_ID);
		dataStore.set("user", credential);		
	}
	
	public String getApplicationFolderId() {
		return gDriveConnectionManager.getApplicationFolderId();
	}

	public GDriveConnectionManager getGDriveConnectionManager() {
		return gDriveConnectionManager;
	}

	public Drive getDrive() {
		return gDriveConnectionManager.getDrive();
	}

	public void connect() throws IOException {
		gDriveConnectionManager.connect(new NullProgressMonitor());
	}

	public void disconnect() throws IOException {
		gDriveConnectionManager.disconnect(new NullProgressMonitor());
	}

	@Override
	protected void after() {
		try {
			if (gDriveConnectionManager == null) {
				return;
			}
			if (user.getCredential().isPresent()) {
				// needed if gDriveConnectionManager.deleteCredentials() has been called
				addCredentialToDataStore(gDriveConnectionManager.getDataStoreDir(), user.getCredential().get());
			}
			connect();
			deleteApplicationFolder();
			disconnect();
			gDriveConnectionManager.close();
		} catch (Exception e) {
			// ignore
		} finally {
			temporaryFolder.delete();
		}
	}

	private void deleteApplicationFolder() throws IOException {
		gDriveConnectionManager.getDrive().files().delete(gDriveConnectionManager.getApplicationFolderId()).execute();
	}

}
