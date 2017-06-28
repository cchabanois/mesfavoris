package mesfavoris.url.internal.handlers;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;
import mesfavoris.url.UrlBookmarkProperties;

public class CopyBookmarkUrlHandlerTest {

	@Test
	public void testCopyUrl() throws ExecutionException {
		// Given
		String url = "https://github.com/cchabanois/mesfavoris";
		Bookmark bookmark = new Bookmark(new BookmarkId(),
				ImmutableMap.of(UrlBookmarkProperties.PROP_URL, url));
		IEvaluationContext context = new EvaluationContext(null, new Object());
		context.addVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME, new StructuredSelection(bookmark));
		ExecutionEvent event = new ExecutionEvent(null, new HashMap<>(), null, context);

		// When
		CopyBookmarkUrlHandler handler = new CopyBookmarkUrlHandler();
		executeHandler(handler, event);

		// Then
		assertEquals(url, getClipboardContents());
	}

	private void executeHandler(IHandler handler, ExecutionEvent event) {
		UIThreadRunnable.syncExec(()->{
			try {
				return handler.execute(event);
			} catch (ExecutionException e) {
				throw new RuntimeException(e);
			}
		});
	}
	
	private String getClipboardContents() {
		return UIThreadRunnable.syncExec(() -> {
			Clipboard clipboard = new Clipboard(null);
			String textData;
			try {
				TextTransfer textTransfer = TextTransfer.getInstance();
				textData = (String) clipboard.getContents(textTransfer);
				return textData;
			} finally {
				clipboard.dispose();
			}
		});
	}	
	
}
