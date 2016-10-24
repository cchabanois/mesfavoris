package mesfavoris.gdrive.operations;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import com.google.api.services.drive.Drive;

import mesfavoris.gdrive.StatusHelper;

public class ViewInGDriveOperation {

	public void viewInGDrive(String fileId) {
		URL url;
		try {
			url = new URL("https://drive.google.com/open?id="+fileId);
			openInExternalBrowser(url);
		} catch (MalformedURLException e) {
			StatusHelper.logError("Could not view gdrive file", e);
			return;
		}
		openInExternalBrowser(url);
	}
	
	private void openInExternalBrowser(URL url) {
		String browserId = "bookmark";
		try {
			IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();

			IWebBrowser browser = browserSupport.createBrowser(IWorkbenchBrowserSupport.AS_EXTERNAL, browserId, null,
					null);
			browser.openURL(url);
		} catch (PartInitException e) {
			StatusHelper.logError("Could not open browser", e);
		}

	}	
	
}
