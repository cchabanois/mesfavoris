package mesfavoris.url;

import static mesfavoris.url.UrlBookmarkProperties.PROP_URL;

import java.net.MalformedURLException;
import java.net.URL;

import org.chabanois.mesfavoris.bookmarktype.IGotoBookmark;
import org.chabanois.mesfavoris.model.Bookmark;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

public class GotoUrlBookmark implements IGotoBookmark {

	@Override
	public boolean gotoBookmark(IWorkbenchWindow window, Bookmark bookmark) {
		String url = bookmark.getPropertyValue(PROP_URL);
		if (url == null) {
			return false;
		}
		try {
			openInExternalBrowser(new URL(url));
			return true;
		} catch (MalformedURLException e) {
			return false;
		}
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
