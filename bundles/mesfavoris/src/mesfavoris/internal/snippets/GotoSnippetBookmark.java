package mesfavoris.internal.snippets;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;

import mesfavoris.bookmarktype.IBookmarkLocation;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.model.Bookmark;
import mesfavoris.ui.dialogs.MesFavorisTextNotificationPopup;

public class GotoSnippetBookmark implements IGotoBookmark {

	@Override
	public boolean gotoBookmark(IWorkbenchWindow window, Bookmark bookmark, IBookmarkLocation bookmarkLocation) {
		if (!(bookmarkLocation instanceof Snippet)) {
			return false;
		}
		Snippet snippet = (Snippet) bookmarkLocation;

		copyToClipboard(window.getWorkbench().getDisplay(), snippet.getContent());

		MesFavorisTextNotificationPopup popup = new MesFavorisTextNotificationPopup(Display.getDefault(),
				"Snippet copied to clipboard");
		popup.open();

		return true;
	}

	private void copyToClipboard(Display display, String text) {
		display.syncExec(() -> {
			Clipboard clipboard = new Clipboard(null);
			try {
				TextTransfer textTransfer = TextTransfer.getInstance();
				Transfer[] transfers = new Transfer[] { textTransfer };
				Object[] data = new Object[] { text };
				clipboard.setContents(data, transfers);
			} finally {
				clipboard.dispose();
			}
		});
	}

}
