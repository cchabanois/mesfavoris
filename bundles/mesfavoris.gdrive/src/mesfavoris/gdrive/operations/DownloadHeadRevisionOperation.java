package mesfavoris.gdrive.operations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;

import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

/**
 * Download a file from GDrive. Also get the corresponding file ETag. 
 * 
 * @author cchabanois
 *
 */
public class DownloadHeadRevisionOperation extends AbstractGDriveOperation {

	public DownloadHeadRevisionOperation(Drive drive) {
		super(drive);
	}

	public Contents downloadFile(String fileId, IProgressMonitor monitor) throws IOException {
		File file = drive.files().get(fileId).execute();
		
		// Don't use Revision revision = drive.revisions().get(fileId, file.getHeadRevisionId()).execute();
		// because this fails when file is shared with read-only role
		
		Drive.Files.Get get = drive.files().get(fileId).setRevisionId(file.getHeadRevisionId());
		MediaHttpDownloader mediaHttpDownloader = get.getMediaHttpDownloader();
		// We should use directDownload but NPE at MediaHttpDownloader:186 if we do
		mediaHttpDownloader.setDirectDownloadEnabled(false);

		FileDownloadProgressListener downloadProgressListener = new FileDownloadProgressListener(
				monitor);
		mediaHttpDownloader.setProgressListener(downloadProgressListener);
		downloadProgressListener.begin();
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			get.executeMediaAndDownloadTo(baos);
			return new Contents(baos.toByteArray(), file.getEtag());
		} finally {
			downloadProgressListener.done();
		}
	}

	public static class Contents {
		private final byte[] fileContents;
		private final String fileEtag;

		public Contents(byte[] fileContents, String fileEtag) {
			this.fileContents = fileContents;
			this.fileEtag = fileEtag;
		}

		public byte[] getFileContents() {
			return fileContents;
		}

		public String getFileEtag() {
			return fileEtag;
		}

	}

}
