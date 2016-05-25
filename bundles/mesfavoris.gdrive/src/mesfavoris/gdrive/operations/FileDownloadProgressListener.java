package mesfavoris.gdrive.operations;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;

import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener;

/**
 * Track progress when downloading a file
 * 
 * @author cchabanois
 *
 */
public class FileDownloadProgressListener implements
		MediaHttpDownloaderProgressListener {
	private final IProgressMonitor monitor;
	private int worked;

	public FileDownloadProgressListener(IProgressMonitor monitor) {
		this.monitor = monitor;
	}

	public void begin() {
		monitor.beginTask("Downloading file", 100);
		worked = 0;
	}

	public void done() {
		monitor.done();
	}

	@Override
	public void progressChanged(MediaHttpDownloader downloader)
			throws IOException {
		switch (downloader.getDownloadState()) {
		case MEDIA_COMPLETE:
			monitor.subTask("Download completed");
			monitor.done();
			break;
		case MEDIA_IN_PROGRESS:
			monitor.subTask("Download in progress");
			int progress = (int) (downloader.getProgress() * 100);
			monitor.worked(progress - worked);
			worked += progress - worked;
			break;
		case NOT_STARTED:
			break;
		default:
			break;

		}

	}

}