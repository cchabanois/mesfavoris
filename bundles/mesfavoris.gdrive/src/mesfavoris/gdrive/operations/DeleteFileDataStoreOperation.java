package mesfavoris.gdrive.operations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.google.api.client.auth.oauth2.StoredCredential;

import mesfavoris.gdrive.connection.GDriveConnectionManager;
import mesfavoris.gdrive.mappings.IBookmarkMappings;
import mesfavoris.remote.IRemoteBookmarksStore.State;

public class DeleteFileDataStoreOperation {
	private final File dataStoreDir;
	private final IBookmarkMappings bookmarkMappings;
	private final GDriveConnectionManager gdriveConnectionManager;

	public DeleteFileDataStoreOperation(File dataStoreDir, GDriveConnectionManager gdriveConnectionManager,
			IBookmarkMappings bookmarkMappings) {
		this.dataStoreDir = dataStoreDir;
		this.gdriveConnectionManager = gdriveConnectionManager;
		this.bookmarkMappings = bookmarkMappings;
	}

	public void deleteDefaultFileDataStore() throws IOException {
		deleteFileDataStore(StoredCredential.DEFAULT_DATA_STORE_ID);
	}

	public void deleteFileDataStore(String id) throws IOException {
		File file = new File(dataStoreDir, id);
		if (!file.exists()) {
			return;
		}
		if (gdriveConnectionManager.getState() != State.disconnected) {
			throw new IOException("Cannot delete file store while connected");
		}
		if (!bookmarkMappings.getMappings().isEmpty()) {
			throw new IOException("Cannot delete file store if there are mappings");			
		}
		Files.delete(file.toPath());
	}

}
