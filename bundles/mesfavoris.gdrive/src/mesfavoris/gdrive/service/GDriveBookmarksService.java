package mesfavoris.gdrive.service;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;

import com.google.api.services.drive.Drive;

import mesfavoris.gdrive.operations.DownloadHeadRevisionOperation;
import mesfavoris.gdrive.operations.DownloadHeadRevisionOperation.FileContents;

public class GDriveBookmarksService {

	public FileContents downloadFile(Drive drive, String fileId, IProgressMonitor monitor) throws IOException {
		DownloadHeadRevisionOperation operation = new DownloadHeadRevisionOperation(drive);
		return operation.downloadFile(fileId, monitor);
	}
	
}
