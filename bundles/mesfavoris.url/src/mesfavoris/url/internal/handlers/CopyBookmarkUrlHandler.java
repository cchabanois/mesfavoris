package mesfavoris.url.internal.handlers;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

import mesfavoris.handlers.AbstractBookmarkHandler;
import mesfavoris.model.Bookmark;
import mesfavoris.url.UrlBookmarkProperties;
import mesfavoris.url.internal.StatusHelper;

public class CopyBookmarkUrlHandler extends AbstractBookmarkHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		String url = getBookmarkUrl(selection);
		if (url == null) {
			return null;
		}

		Clipboard clipboard = new Clipboard(null);
		try {
			TextTransfer textTransfer = TextTransfer.getInstance();
			Transfer[] transfers = new Transfer[] { textTransfer };
			Object[] data = new Object[] { url };
			clipboard.setContents(data, transfers);
			displayNotification(url);
		} finally {
			clipboard.dispose();
		}
		return null;
	}

	private void displayNotification(String url) {
		Display.getDefault().asyncExec(() -> {
			try {
				UrlCopiedNotificationPopup popup = new UrlCopiedNotificationPopup(Display.getDefault(), new URL(url));
				popup.open();
			} catch (MalformedURLException e) {
				StatusHelper.logError("Cannot display notification", e);
			}
		});
	}

	private String getBookmarkUrl(IStructuredSelection selection) {
		Bookmark bookmark = getSelectedBookmark(selection);
		if (bookmark == null) {
			return null;
		}
		String url = bookmark.getPropertyValue(UrlBookmarkProperties.PROP_URL);
		return url;
	}

	private Bookmark getSelectedBookmark(IStructuredSelection selection) {
		if (selection.isEmpty()) {
			return null;
		}
		Bookmark bookmark = (Bookmark) selection.getFirstElement();
		return bookmark;
	}

	@Override
	public boolean isEnabled() {
		IStructuredSelection selection = getSelection();
		String url = getBookmarkUrl(selection);
		return url != null;
	}
}
