package mesfavoris.url;

import java.net.URL;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import mesfavoris.bookmarktype.IBookmarkLocation;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.model.Bookmark;

public class GotoUrlBookmark implements IGotoBookmark {

	@Override
	public boolean gotoBookmark(IWorkbenchWindow window, Bookmark bookmark, IBookmarkLocation bookmarkLocation) {
		if (!(bookmarkLocation instanceof UrlBookmarkLocation)) {
			return false;
		}
		UrlBookmarkLocation urlBookmarkLocation = (UrlBookmarkLocation) bookmarkLocation;
		openInExternalBrowser(urlBookmarkLocation.getUrl());
		return true;
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
