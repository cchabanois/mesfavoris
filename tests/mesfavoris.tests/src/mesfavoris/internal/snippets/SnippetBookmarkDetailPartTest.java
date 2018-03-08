package mesfavoris.internal.snippets;

import static mesfavoris.internal.snippets.SnippetBookmarkProperties.PROP_SNIPPET_CONTENT;
import static mesfavoris.model.Bookmark.PROPERTY_NAME;
import static mesfavoris.tests.commons.bookmarks.MainBookmarkDatabaseHelper.addBookmark;
import static mesfavoris.tests.commons.bookmarks.MainBookmarkDatabaseHelper.getBookmarksRootFolderId;
import static mesfavoris.tests.commons.bookmarks.MainBookmarkDatabaseHelper.getBookmarksTree;
import static mesfavoris.tests.commons.waits.Waiter.waitUntil;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;
import mesfavoris.tests.commons.ui.AbstractControlTest;

public class SnippetBookmarkDetailPartTest extends AbstractControlTest {
	private SnippetBookmarkDetailPart bookmarkDetailsPart;
	private FormToolkit formToolkit;

	@Before
	public void setUp() {
		bookmarkDetailsPart = new SnippetBookmarkDetailPart();
		formToolkit = UIThreadRunnable.syncExec(() -> new FormToolkit(Display.getDefault()));
		openShell(shell -> {
			bookmarkDetailsPart.createControl(shell, formToolkit);
		}, 200, 200);
	}

	@After
	public void tearDown() {
		bookmarkDetailsPart.dispose();
		formToolkit.dispose();
	}

	@Test
	public void testNameUpdatedWhenSnippetIsEdited() throws Exception {
		// Given
		Bookmark bookmark = new Bookmark(new BookmarkId(),
				ImmutableMap.of(PROPERTY_NAME, "my snippet", PROP_SNIPPET_CONTENT, "my snippet"));
		addBookmark(getBookmarksRootFolderId(), bookmark);
		setBookmark(bookmark);
		SWTBotStyledText snippetControl = bot.styledText();
		assertThat(snippetControl.getText()).isEqualTo("my snippet");

		// When
		snippetControl.insertText(0, 0, "edited ");

		// Then
		waitUntil("Snippet content not updated", () -> "edited my snippet"
				.equals(getBookmarksTree().getBookmark(bookmark.getId()).getPropertyValue(PROP_SNIPPET_CONTENT)));
		waitUntil("Bookmark name not updated", () -> "edited my snippet"
				.equals(getBookmarksTree().getBookmark(bookmark.getId()).getPropertyValue(PROPERTY_NAME)));
	}

	@Test
	public void testNameNotUpdatedIfDifferentFromFirstSnippetLine() throws Exception {
		// Given
		Bookmark bookmark = new Bookmark(new BookmarkId(),
				ImmutableMap.of(PROPERTY_NAME, "the name", PROP_SNIPPET_CONTENT, "my snippet"));
		addBookmark(getBookmarksRootFolderId(), bookmark);
		setBookmark(bookmark);
		SWTBotStyledText snippetControl = bot.styledText();
		assertThat(snippetControl.getText()).isEqualTo("my snippet");

		// When
		snippetControl.insertText(0, 0, "edited ");

		// Then
		waitUntil("Snippet content not updated", () -> "edited my snippet"
				.equals(getBookmarksTree().getBookmark(bookmark.getId()).getPropertyValue(PROP_SNIPPET_CONTENT)));
		assertEquals("the name", bookmark.getPropertyValue(PROPERTY_NAME));
	}

	private void setBookmark(Bookmark bookmark) {
		UIThreadRunnable.asyncExec(() -> bookmarkDetailsPart.setBookmark(bookmark));
	}
}
