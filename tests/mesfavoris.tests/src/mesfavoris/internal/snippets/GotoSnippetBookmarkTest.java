package mesfavoris.internal.snippets;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.junit.Test;

import mesfavoris.bookmarktype.IBookmarkLocation;
import mesfavoris.model.Bookmark;

public class GotoSnippetBookmarkTest {
	private final GotoSnippetBookmark gotoBookmark = new GotoSnippetBookmark();

	@Test
	public void testGotoSnippet() {
		// Given
		String contents = "My snippet contents";
		Snippet snippet = new Snippet(contents);
		
		// When
		gotoBookmark(null, snippet);
		
		// Then
		assertThat(getTextFromClipboard()).hasValue(contents);
	}

	private boolean gotoBookmark(Bookmark bookmark, IBookmarkLocation location) {
		return UIThreadRunnable.syncExec(() -> gotoBookmark.gotoBookmark(getActiveWindow(), bookmark, location));
	}

	private IWorkbenchWindow getActiveWindow() {
		return UIThreadRunnable.syncExec(() -> PlatformUI.getWorkbench().getActiveWorkbenchWindow());
	}

	private Optional<String> getTextFromClipboard() {
		return UIThreadRunnable.syncExec(() -> {
			Display display = PlatformUI.getWorkbench().getDisplay();
			Clipboard clipboard = new Clipboard(display);
			try {
				String text = (String) clipboard.getContents(TextTransfer.getInstance());
				return Optional.ofNullable(text);
			} finally {
				clipboard.dispose();
			}
		});
	}

}
