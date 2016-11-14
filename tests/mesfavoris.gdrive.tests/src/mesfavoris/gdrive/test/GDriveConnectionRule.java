package mesfavoris.gdrive.test;

import java.io.IOException;
import java.util.Random;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

import com.google.api.services.drive.Drive;

import mesfavoris.gdrive.GDriveTestUser;
import mesfavoris.gdrive.connection.GDriveConnectionManager;

public class GDriveConnectionRule extends ExternalResource {

	private GDriveConnectionManager gDriveConnectionManager;
	private final TemporaryFolder temporaryFolder = new TemporaryFolder();
	private final boolean connect;
	private final HtmlUnitAuthorizationCodeInstalledApp.Provider authorizationCodeProvider;
	
	public GDriveConnectionRule(GDriveTestUser user, boolean connect) {
		this.connect = connect;
		this.authorizationCodeProvider = new HtmlUnitAuthorizationCodeInstalledApp.Provider(user.getUserName(),
				user.getPassword(), user.getRecoveryEmail());
	}

	@Override
	protected void before() throws Throwable {
		temporaryFolder.create();
		java.io.File dataStoreDir = temporaryFolder.newFolder();
		String applicationFolderName = "gdriveConnectionManagerTest" + new Random().nextInt(1000);
		gDriveConnectionManager = new GDriveConnectionManager(dataStoreDir, authorizationCodeProvider, "mes favoris",
				applicationFolderName);
		gDriveConnectionManager.init();
		if (connect) {
			connect();
		}
	}

	public void setAuthorizationListener(IAuthorizationListener authorizationListener) {
		this.authorizationCodeProvider.setAuthorizationListener(authorizationListener);
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
