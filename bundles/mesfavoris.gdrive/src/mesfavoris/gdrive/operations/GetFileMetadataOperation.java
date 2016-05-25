package mesfavoris.gdrive.operations;

import java.io.IOException;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

public class GetFileMetadataOperation extends AbstractGDriveOperation {

	public GetFileMetadataOperation(Drive drive) {
		super(drive);
	}

	public File getFileMetadata(String fileId) throws IOException {
		Drive.Files.Get get = drive.files().get(fileId);
		return get.execute();
	}
}
