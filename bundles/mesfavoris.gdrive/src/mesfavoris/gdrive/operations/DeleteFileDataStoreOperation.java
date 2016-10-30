package mesfavoris.gdrive.operations;

import java.io.IOException;

import mesfavoris.gdrive.connection.GDriveConnectionManager;
import mesfavoris.gdrive.mappings.IBookmarkMappings;

public class DeleteFileDataStoreOperation {
	private final IBookmarkMappings bookmarkMappings;
	private final GDriveConnectionManager gdriveConnectionManager;

	public DeleteFileDataStoreOperation(GDriveConnectionManager gdriveConnectionManager,
			IBookmarkMappings bookmarkMappings) {
		this.gdriveConnectionManager = gdriveConnectionManager;
		this.bookmarkMappings = bookmarkMappings;
	}

	public void deleteFileDataStore() throws IOException {
		if (!bookmarkMappings.getMappings().isEmpty()) {
			throw new IOException("Cannot delete file store if there are mappings");
		}
		gdriveConnectionManager.deleteCredentials();
	}

}
