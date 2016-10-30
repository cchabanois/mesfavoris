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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.SubMonitor;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
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
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.User;

import mesfavoris.gdrive.StatusHelper;
import mesfavoris.gdrive.connection.auth.AuthorizationCodeEclipseApp;
import mesfavoris.gdrive.connection.auth.CancellableLocalServerReceiver;
import mesfavoris.gdrive.connection.auth.IAuthorizationCodeInstalledAppProvider;
import mesfavoris.remote.IRemoteBookmarksStore.State;
import mesfavoris.remote.UserInfo;

/**
 * Manages connection to GDrive. A folder is created for the application.
 * 
 * @author cchabanois
 *
 */
public class GDriveConnectionManager {
	public static final String USER_FILENAME = "user.json";

	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	private final ListenerList connectionListenerList = new ListenerList();
	private HttpTransport httpTransport;
	private final String applicationName;
	private final File dataStoreDir;
	private final IAuthorizationCodeInstalledAppProvider authorizationCodeInstalledAppProvider;
	private final String applicationFolderName;
	private final AtomicReference<State> state = new AtomicReference<State>(State.disconnected);
	private Drive drive;
	private String applicationFolderId;
	private UserInfo userInfo;

	/**
	 * 
	 * @param dataStoreDir
	 *            directory for credential store
	 * @param applicationName
	 *            the application name to be used in the UserAgent header of
	 *            each request
	 * @param applicationFolderName
	 *            the folder name for the application on GDrive. It will be
	 *            created after connection if it does not exist
	 */
	public GDriveConnectionManager(File dataStoreDir, String applicationName, String applicationFolderName) {
		this(dataStoreDir, new AuthorizationCodeEclipseApp.Provider(), applicationName, applicationFolderName);
	}

	public GDriveConnectionManager(File dataStoreDir,
			IAuthorizationCodeInstalledAppProvider authorizationCodeInstalledAppProvider, String applicationName,
			String applicationFolderName) {
		this.dataStoreDir = dataStoreDir;
		this.authorizationCodeInstalledAppProvider = authorizationCodeInstalledAppProvider;
		this.applicationName = applicationName;
		this.applicationFolderName = applicationFolderName;
	}

	public void init() throws GeneralSecurityException, IOException {
		httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		UserInfo user = loadUserInfo();
		synchronized (this) {
			this.userInfo = user;
		}
	}

	public void close() throws IOException {
		httpTransport.shutdown();
	}

	public String getApplicationFolderName() {
		return applicationFolderName;
	}

	public File getDataStoreDir() {
		return dataStoreDir;
	}

	/**
	 * Get info about user currently associated to the manager
	 * 
	 * @return the user or null if unknown
	 */
	public synchronized UserInfo getUserInfo() {
		return userInfo;
	}

	public void connect(IProgressMonitor monitor) throws IOException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
		if (!state.compareAndSet(State.disconnected, State.connecting)) {
			return;
		}
		try {
			Credential credential = authorize(subMonitor.newChild(85));
			Drive drive = new Drive.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(applicationName)
					.build();
			String bookmarkDirId = getApplicationFolderId(drive);
			UserInfo authenticatedUser = getAuthenticatedUser(drive, subMonitor.newChild(10));
			saveUser(authenticatedUser, subMonitor.newChild(5));
			synchronized (this) {
				this.drive = drive;
				this.applicationFolderId = bookmarkDirId;
				this.userInfo = authenticatedUser;
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
			// In this context, the client secret is obviously not treated as a
			// secret.
			GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
					new InputStreamReader(GDriveConnectionManager.class.getResourceAsStream("client_secrets.json")));
			// set up authorization code flow
			FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(dataStoreDir);
			GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY,
					clientSecrets, Collections.singleton(DriveScopes.DRIVE)).setDataStoreFactory(dataStoreFactory)
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

	private UserInfo getAuthenticatedUser(Drive drive, IProgressMonitor monitor) throws IOException {
		SubMonitor subMonitor = SubMonitor.convert(monitor);
		About about = drive.about().get().execute();
		User user = about.getUser();
		return new UserInfo(user.getEmailAddress(), user.getDisplayName());
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

	private UserInfo loadUserInfo() {
		UserInfoPersister persister = new UserInfoPersister(new File(dataStoreDir, USER_FILENAME));
		try {
			return persister.loadUser(new NullProgressMonitor());
		} catch (IOException e) {
			StatusHelper.logWarn("Could not load gdrive user info", e);
			return null;
		}
	}

	private void saveUser(UserInfo user, IProgressMonitor monitor) {
		UserInfoPersister persister = new UserInfoPersister(new File(dataStoreDir, USER_FILENAME));
		try {
			persister.saveUser(user, monitor);
		} catch (IOException e) {
			StatusHelper.logWarn("Could not save gdrive user info", e);
		}
	}
	
	public void deleteCredentials() throws IOException {
		if (getState() != State.disconnected) {
			throw new IOException("Cannot delete file store while connected");
		}
		synchronized(this) {
			this.userInfo = null;
		}
		File file = new File(dataStoreDir, StoredCredential.DEFAULT_DATA_STORE_ID);
		if (file.exists()) {
			java.nio.file.Files.delete(file.toPath());
		}
		File userInfoFile = new File(dataStoreDir, GDriveConnectionManager.USER_FILENAME);
		if (userInfoFile.exists()) {
			java.nio.file.Files.delete(userInfoFile.toPath());
		}	
	}
}
