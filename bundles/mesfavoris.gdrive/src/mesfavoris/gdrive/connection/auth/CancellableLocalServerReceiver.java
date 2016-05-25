package mesfavoris.gdrive.connection.auth;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;

import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;

public class CancellableLocalServerReceiver extends LocalServerReceiver {
	private final IProgressMonitor monitor;

	public CancellableLocalServerReceiver(IProgressMonitor monitor) {
		this.monitor = monitor;
	}

	@Override
	public String waitForCode() throws IOException {
		lock.lock();
		try {
			while (code == null && error == null && !monitor.isCanceled()) {
				try {
					gotAuthorizationResponse.await(500, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
				}
			}
			if (monitor.isCanceled()) {
				error = "Canceled";
			}
			if (error != null) {
				throw new IOException("User authorization failed (" + error
						+ ")");
			}
			return code;
		} finally {
			lock.unlock();
		}
	}

}
