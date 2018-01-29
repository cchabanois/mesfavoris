package mesfavoris.url.internal.handlers;

import java.net.URL;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import mesfavoris.ui.dialogs.AbstractMesFavorisNotificationPopup;
import mesfavoris.url.internal.StatusHelper;

public class UrlCopiedNotificationPopup extends AbstractMesFavorisNotificationPopup {
	private final URL url;
	
	public UrlCopiedNotificationPopup(Display display, URL url) {
		super(display);
		this.url = url;
	}

	@Override
	protected void createContentArea(Composite composite) {
		composite.setLayout(new GridLayout(1, true));
		Link link = new Link(composite, 0);
		String text = "<a href=\""+url+"\">Url</a> copied to clipboard";
		link.setText(text);

		link.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				openInExternalBrowser();
			}
		});
	}
	
	private void openInExternalBrowser() {
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