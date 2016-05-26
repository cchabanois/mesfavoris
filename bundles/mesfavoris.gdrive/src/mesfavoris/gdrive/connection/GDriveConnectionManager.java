package mesfavoris.gdrive.connection;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;

import mesfavoris.gdrive.StatusHelper;
import mesfavoris.gdrive.connection.auth.AuthorizationCodeEclipseApp;
import mesfavoris.gdrive.connection.auth.CancellableLocalServerReceiver;
import mesfavoris.gdrive.connection.auth.IAuthorizationCodeInstalledAppProvider;
import mesfavoris.remote.IConnectionListener;
import mesfavoris.remote.IRemoteBookmarksStore.State;

/**
 * Manages connection to GDrive. A folder is created for the application.
 * 
 * @author cchabanois
 *
 */
public class GDriveConnectionManager {
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	private final ListenerList connectionListenerList = new ListenerList();
	private HttpTransport httpTransport;

	private final File dataStoreDir;
	private final IAuthorizationCodeInstalledAppProvider authorizationCodeInstalledAppProvider;
	private final String applicationFolderName;
	private FileDataStoreFactory dataStoreFactory;
	private final AtomicReference<State> state = new AtomicReference<State>(State.disconnected);
	private Drive drive;
	private String applicationFolderId;

	public GDriveConnectionManager(File dataStoreDir, String applicationFolderName) {
		this(dataStoreDir, new AuthorizationCodeEclipseApp.Provider(), applicationFolderName);
	}

	public GDriveConnectionManager(File dataStoreDir,
			IAuthorizationCodeInstalledAppProvider authorizationCodeInstalledAppProvider, String applicationFolderName) {
		this.dataStoreDir = dataStoreDir;
		this.authorizationCodeInstalledAppProvider = authorizationCodeInstalledAppProvider;
		this.applicationFolderName = applicationFolderName;
	}

	public void init() throws GeneralSecurityException, IOException {
		httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		dataStoreFactory = new FileDataStoreFactory(dataStoreDir);
	}

	public void close() throws IOException {
		httpTransport.shutdown();
	}

	public String getApplicationFolderName() {
		return applicationFolderName;
	}
	
	public void connect(IProgressMonitor monitor) throws IOException {
		if (!state.compareAndSet(State.disconnected, State.connecting)) {
			return;
		}
		try {
			Credential credential = authorize(monitor);
			Drive drive = new Drive.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(null).build();
			String bookmarkDirId = getApplicationFolderId(drive);
			synchronized (this) {
				this.drive = drive;
				this.applicationFolderId = bookmarkDirId;
			}
			state.set(State.connected);
			fireConnected(null);
		} finally {
			if (state.compareAndSet(State.connecting, State.disconnected)) {
				synchronized (this) {
					this.drive = null;
					this.applicationFolderId = null;
				}
			}
		}
	}

	public synchronized Drive getDrive() {
		return drive;
	}

	public synchronized String getApplicationFolderId() {
		return applicationFolderId;
	}

	private String getApplicationFolderId(Drive drive) throws IOException {
		Files.List request = drive.files().list()
				.setQ("mimeType='application/vnd.google-apps.folder' and trashed=false and title='"
						+ applicationFolderName + "' and 'root' in parents");
		FileList files = request.execute();
		if (files.getItems().isEmpty()) {
			return createApplicationFolder(drive);
		} else {
			return files.getItems().get(0).getId();
		}
	}

	private String createApplicationFolder(Drive drive) throws IOException {
		com.google.api.services.drive.model.File body = new com.google.api.services.drive.model.File();
		body.setTitle(applicationFolderName);
		body.setMimeType("application/vnd.google-apps.folder");
		com.google.api.services.drive.model.File file = drive.files().insert(body).execute();
		return file.getId();
	}

	public State getState() {
		return state.get();
	}

	private void fireConnected(final Drive drive) {
		Object[] listeners = connectionListenerList.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			final IConnectionListener listener = (IConnectionListener) listeners[i];
			SafeRunner.run(new ISafeRunnable() {

				public void run() throws Exception {
					listener.connected();

				}

				public void handleException(Throwable exception) {
					StatusHelper.logError("Error when drive connected", exception);
				}
			});
		}
	}

	private void fireDisconnected(final Drive drive) {
		Object[] listeners = connectionListenerList.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			final IConnectionListener listener = (IConnectionListener) listeners[i];
			SafeRunner.run(new ISafeRunnable() {

				public void run() throws Exception {
					listener.disconnected();

				}

				public void handleException(Throwable exception) {
					StatusHelper.logError("Error when drive disconnected", exception);
				}
			});
		}
	}

	private Credential authorize(final IProgressMonitor monitor) throws IOException {
		try {
			monitor.beginTask("Authorizes the application to access user's protected data on GDrive", 100);
			// load client secrets
			//  In this context, the client secret is obviously not treated as a secret.
			GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
					new InputStreamReader(GDriveConnectionManager.class.getResourceAsStream("client_secrets.json")));
			// set up authorization code flow
			GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY,
					clientSecrets, Collections.singleton(DriveScopes.DRIVE_FILE)).setDataStoreFactory(dataStoreFactory)
							.build();
			// authorize
			LocalServerReceiver localServerReceiver = new CancellableLocalServerReceiver(monitor);
			AuthorizationCodeInstalledApp authorizationCodeInstalledApp = authorizationCodeInstalledAppProvider
					.get(flow, localServerReceiver, monitor);
			return authorizationCodeInstalledApp.authorize("user");
		} finally {
			monitor.done();
		}
	}

	public void disconnect(IProgressMonitor monitor) throws IOException {
		if (!state.compareAndSet(State.connected, State.disconnected)) {
			return;
		}
		synchronized (this) {
			this.drive = null;
			this.applicationFolderId = null;
		}
		fireDisconnected(null);
	}

	public void addConnectionListener(IConnectionListener listener) {
		connectionListenerList.add(listener);
	}

	public void removeConnectionListener(IConnectionListener listener) {
		connectionListenerList.remove(listener);
	}

}
