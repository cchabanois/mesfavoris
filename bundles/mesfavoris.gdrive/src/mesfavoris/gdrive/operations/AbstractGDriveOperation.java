package mesfavoris.gdrive.operations;

import com.google.api.services.drive.Drive;

public abstract class AbstractGDriveOperation {
	public static final String MIME_TYPE = "application/vnd.mesfavoris-eclipse";
	protected final Drive drive;
	
	public AbstractGDriveOperation(Drive drive) {
		this.drive = drive;
	}

}
