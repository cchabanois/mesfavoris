package mesfavoris.gdrive.operations;

import java.io.IOException;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;

public class ShareFileOperation extends AbstractGDriveOperation {

	public ShareFileOperation(Drive drive) {
		super(drive);
	}

	public void shareWithUser(String fileId, String userEmail, boolean canWrite) throws IOException {
		Permission permission = new Permission();

		permission.setValue(userEmail);
		permission.setType("user");
		permission.setRole(canWrite ? "writer" : "reader");

		drive.permissions().insert(fileId, permission).execute();
	}

	public void shareWithAnyone(String fileId, boolean canWrite, boolean withLink) throws IOException {
		Permission permission = new Permission();

		permission.setValue("");
		permission.setType("anyone");
		permission.setRole(canWrite ? "writer" : "reader");
		permission.setWithLink(withLink);
		
		drive.permissions().insert(fileId, permission).execute();
	}

}
