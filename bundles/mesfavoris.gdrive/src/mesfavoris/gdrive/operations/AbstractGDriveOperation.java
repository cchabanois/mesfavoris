package mesfavoris.gdrive.operations;

import com.google.api.services.drive.Drive;

public abstract class AbstractGDriveOperation {
	protected final Drive drive;
	
	public AbstractGDriveOperation(Drive drive) {
		this.drive = drive;
	}

}
