package mesfavoris.gdrive.operations;

import java.io.IOException;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

public class TrashFileOperation extends AbstractGDriveOperation {

	public TrashFileOperation(Drive drive) {
		super(drive);
	}

	public File trashFile(String fileId) throws IOException {
		Drive.Files.Trash trash = drive.files().trash(fileId);
		
		return trash.execute();
	}	
	
}
