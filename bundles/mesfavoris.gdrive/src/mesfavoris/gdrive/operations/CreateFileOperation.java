package mesfavoris.gdrive.operations;

import java.io.IOException;
import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;

import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;

/**
 * Create a file on GDrive
 * 
 * @author cchabanois
 *
 */
public class CreateFileOperation extends AbstractGDriveOperation {
	public CreateFileOperation(Drive drive) {
		super(drive);
	}

	public File createFile(String parentId, String name, byte[] content, IProgressMonitor monitor) throws IOException {
		File fileMetadata = new File();
		fileMetadata.setTitle(name);
		fileMetadata.setParents(Arrays.asList(new ParentReference().setId(parentId)));
		ByteArrayContent mediaContent = new ByteArrayContent(MIME_TYPE, content);

		Drive.Files.Insert insert = drive.files().insert(fileMetadata, mediaContent);
		MediaHttpUploader uploader = insert.getMediaHttpUploader();
		uploader.setDirectUploadEnabled(true);

		FileUploadProgressListener uploadProgressListener = new FileUploadProgressListener(monitor);
		uploader.setProgressListener(uploadProgressListener);
		uploadProgressListener.begin();
		try {
			fileMetadata = insert.execute();
			return fileMetadata;
		} finally {
			uploadProgressListener.done();
		}
	}

}
