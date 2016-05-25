package mesfavoris.gdrive.operations;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;

import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;

/**
 * Track progress when uploading a file.
 * 
 * @author cchabanois
 *
 */
public class FileUploadProgressListener implements MediaHttpUploaderProgressListener {
	private final IProgressMonitor monitor;
	private int worked;

	public FileUploadProgressListener(IProgressMonitor monitor) {
		this.monitor = monitor;
	}

	public void begin() {
		monitor.beginTask("Uploading file", 100);
	}

	public void done() {
		monitor.done();
	}

	@Override
	public void progressChanged(MediaHttpUploader uploader) throws IOException {
		switch (uploader.getUploadState()) {
		case NOT_STARTED:
			break;
		case INITIATION_STARTED:
			worked = 0;
			monitor.subTask("Upload Initiation has started");
			break;
		case INITIATION_COMPLETE:
			monitor.subTask("Upload Initiation is complete");
			break;
		case MEDIA_IN_PROGRESS:
			monitor.subTask("Upload in progress");
			int progress = (int) (uploader.getProgress() * 100);
			monitor.worked(progress - worked);
			worked += progress - worked;
			break;
		case MEDIA_COMPLETE:
			monitor.subTask("Upload completed");
			monitor.done();
			break;

		default:
			break;
		}
	}
}