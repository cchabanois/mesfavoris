package mesfavoris.gdrive.operations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;

import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Revision;

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
		Revision revision = drive.revisions().get(fileId, file.getHeadRevisionId()).execute();

		HttpRequestFactory requestFactory = drive.getRequestFactory();
		MediaHttpDownloader mediaHttpDownloader = new MediaHttpDownloader(requestFactory.getTransport(),
				requestFactory.getInitializer());
		mediaHttpDownloader.setDirectDownloadEnabled(true);

		FileDownloadProgressListener downloadProgressListener = new FileDownloadProgressListener(monitor);
		mediaHttpDownloader.setProgressListener(downloadProgressListener);
		downloadProgressListener.begin();
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			mediaHttpDownloader.download(new GenericUrl(revision.getDownloadUrl()), baos);
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
