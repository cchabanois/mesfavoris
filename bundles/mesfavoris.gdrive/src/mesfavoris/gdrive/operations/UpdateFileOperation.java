package mesfavoris.gdrive.operations;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

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
	private final Duration durationForNewRevision;
	private final Clock clock;
	
	public UpdateFileOperation(Drive drive) {
		this(drive, Duration.ofMillis(0));
	}
	
	public UpdateFileOperation(Drive drive, Duration durationForNewRevision) {
		this(drive, Clock.systemUTC(), durationForNewRevision);
	}

	public UpdateFileOperation(Drive drive, Clock clock, Duration durationForNewRevision) {
		super(drive);
		this.durationForNewRevision = durationForNewRevision;
		this.clock = clock;
	}	
	
	public File updateFile(String fileId, String mimeType, byte[] content, String etag,
			IProgressMonitor monitor) throws IOException {
		boolean needsNewRevision = needsNewRevision(fileId);
		File fileMetadata = new File();
		ByteArrayContent mediaContent = new ByteArrayContent(mimeType, content);
		Drive.Files.Update update = drive.files().update(fileId, fileMetadata,
				mediaContent);
		update.setNewRevision(needsNewRevision);
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
	
	private boolean needsNewRevision(String fileId) throws IOException {
		if (durationForNewRevision.isZero()) {
			return true;
		}
		Drive.Files.Get get = drive.files().get(fileId);
		File latestFileVersion = get.execute();
		if (!latestFileVersion.getLastModifyingUser().getIsAuthenticatedUser()) {
			return true;
		}
		Instant modifiedInstant = Instant.ofEpochMilli(latestFileVersion.getModifiedDate().getValue());
		return clock.instant().isAfter(modifiedInstant.plus(durationForNewRevision));
	}
}
