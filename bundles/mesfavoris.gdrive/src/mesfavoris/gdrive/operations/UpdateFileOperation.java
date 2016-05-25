package mesfavoris.gdrive.operations;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;

import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

/**
 * Update a GDrive file
 * 
 * @author cchabanois
 *
 */
public class UpdateFileOperation extends AbstractGDriveOperation {

	public UpdateFileOperation(Drive drive) {
		super(drive);
	}

	public File updateFile(String fileId, byte[] content, String etag,
			IProgressMonitor monitor) throws IOException {
		File fileMetadata = new File();
		ByteArrayContent mediaContent = new ByteArrayContent(MIME_TYPE, content);
		Drive.Files.Update update = drive.files().update(fileId, fileMetadata,
				mediaContent);
		if (etag != null) {
			HttpHeaders headers = update.getRequestHeaders();
			headers.setIfMatch(etag);
			update.setRequestHeaders(headers);
		}
		MediaHttpUploader uploader = update.getMediaHttpUploader();
		uploader.setDirectUploadEnabled(true);

		FileUploadProgressListener uploadProgressListener = new FileUploadProgressListener(
				monitor);
		uploader.setProgressListener(uploadProgressListener);
		uploadProgressListener.begin();
		try {			
			fileMetadata = update.execute();
			return fileMetadata;
		} finally {
			uploadProgressListener.done();
		}
	}
}
