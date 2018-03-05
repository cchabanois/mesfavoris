package mesfavoris.internal.views.details;

import static mesfavoris.internal.snippets.SnippetBookmarkProperties.PROP_SNIPPET_CONTENT;
import static mesfavoris.model.Bookmark.PROPERTY_COMMENT;
import static mesfavoris.model.Bookmark.PROPERTY_NAME;
import static mesfavoris.tests.commons.bookmarks.MainBookmarkDatabaseHelper.addBookmark;
import static mesfavoris.tests.commons.bookmarks.MainBookmarkDatabaseHelper.deleteBookmark;
import static mesfavoris.tests.commons.bookmarks.MainBookmarkDatabaseHelper.getBookmarksRootFolderId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;

import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import mesfavoris.BookmarksException;
import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;
import mesfavoris.tests.commons.ui.AbstractControlTest;

public class BookmarkDetailsPartTest extends AbstractControlTest {
	private BookmarkDetailsPart bookmarkDetailsPart;
	private FormToolkit formToolkit;

	@Before
	public void setUp() {
		bookmarkDetailsPart = new BookmarkDetailsPart(
				BookmarksPlugin.getDefault().getPluginBookmarkTypes().getBookmarkDetailParts());
		formToolkit = UIThreadRunnable.syncExec(() -> new FormToolkit(Display.getDefault()));
		openShell(shell -> {
			bookmarkDetailsPart.createControl(shell, formToolkit);
		}, 200, 200);
	}

	@After
	public void tearDown() {
		formToolkit.dispose();
	}

	@Test
	public void testNoBookmarkSelected() {
		// Given
		Bookmark bookmark = null;

		// When
		setBookmark(bookmark);

		// Then
		assertThat(tabItemCount()).isEqualTo(2);
		bot.cTabItem("Comments").activate();
		SWTBotStyledText commentsControl = bot.styledText();
		assertThat(commentsControl.getText()).isEmpty();
		assertThat(UIThreadRunnable.syncExec(() -> commentsControl.widget.getEditable())).isFalse();

		bot.cTabItem("Properties").activate();
		SWTBotTree treeControl = bot.tree();
		assertThat(treeControl.rowCount()).isEqualTo(0);
	}

	private void setBookmark(Bookmark bookmark) {
		UIThreadRunnable.asyncExec(() -> bookmarkDetailsPart.setBookmark(bookmark));
	}

	@Test
	public void testVirtualBookmarkSelected() {
		// Given
		Bookmark bookmark = new Bookmark(new BookmarkId(), ImmutableMap.of(PROPERTY_NAME, "Recent bookmarks"));

		setBookmark(bookmark);

		// Then
		assertThat(tabItemCount()).isEqualTo(2);
		bot.cTabItem("Comments").activate();
		SWTBotStyledText commentsControl = bot.styledText();
		assertThat(commentsControl.getText()).isEmpty();
		assertThat(UIThreadRunnable.syncExec(() -> commentsControl.widget.getEditable())).isFalse();

		bot.cTabItem("Properties").activate();
		SWTBotTree treeControl = bot.tree();
		assertThat(treeControl.rowCount()).isEqualTo(0);
	}

	@Test
	public void testBookmarkWithCommentSelected() throws BookmarksException {
		// Given
		Bookmark bookmark = new Bookmark(new BookmarkId(),
				ImmutableMap.of(PROPERTY_NAME, "my bookmark", PROPERTY_COMMENT, "my comment"));
		addBookmark(getBookmarksRootFolderId(), bookmark);

		// When
		setBookmark(bookmark);

		// Then
		assertThat(tabItemCount()).isEqualTo(2);
		bot.cTabItem("Comments").activate();
		SWTBotStyledText commentsControl = bot.styledText();
		assertThat(commentsControl.getText()).isEqualTo("my comment");
		assertThat(UIThreadRunnable.syncExec(() -> commentsControl.widget.getEditable())).isTrue();

		bot.cTabItem("Properties").activate();
		SWTBotTreeItem treeItem = bot.tree().expandNode("default");
		assertThat(treeItem.getNode(PROPERTY_COMMENT).cell(1)).isEqualTo("my comment");
		assertThat(treeItem.getNode(PROPERTY_NAME).cell(1)).isEqualTo("my bookmark");
	}

	private int tabItemCount() {
		return bot.widgets(widgetOfType(CTabItem.class)).size();
	}

	@Test
	public void testBookmarkWithSnippetSelected() throws BookmarksException {
		// Given
		Bookmark bookmark = new Bookmark(new BookmarkId(),
				ImmutableMap.of(PROPERTY_NAME, "my bookmark", PROP_SNIPPET_CONTENT, "my snippet"));
		addBookmark(getBookmarksRootFolderId(), bookmark);

		// When
		setBookmark(bookmark);

		// Then
		assertThat(tabItemCount()).isEqualTo(3);
		bot.cTabItem("Comments").activate();
		SWTBotStyledText commentsControl = bot.styledText();
		assertThat(commentsControl.getText()).isEqualTo("");
		assertThat(UIThreadRunnable.syncExec(() -> commentsControl.widget.getEditable())).isTrue();

		bot.cTabItem("Snippet").activate();
		SWTBotStyledText snippetControl = bot.styledText();
		assertThat(snippetControl.getText()).isEqualTo("my snippet");
		assertThat(UIThreadRunnable.syncExec(() -> snippetControl.widget.getEditable())).isTrue();

		bot.cTabItem("Properties").activate();
		SWTBotTreeItem treeItem = bot.tree().expandNode("default");
		treeItem.getNode(PROPERTY_NAME);
		treeItem = bot.tree().expandNode("snippet");
		assertThat(treeItem.getNode(PROP_SNIPPET_CONTENT).cell(1)).isEqualTo("my snippet");
	}

	@Test
	public void testDeleteSelectedBookmark() throws BookmarksException {
		// Given
		Bookmark bookmark = new Bookmark(new BookmarkId(), ImmutableMap.of(PROPERTY_NAME, "my bookmark",
				PROPERTY_COMMENT, "my comment", PROP_SNIPPET_CONTENT, "my snippet"));
		addBookmark(getBookmarksRootFolderId(), bookmark);
		setBookmark(bookmark);
		bot.cTabItem("Comments");
		bot.cTabItem("Properties");
		bot.cTabItem("Snippet");
		
		// When
		deleteBookmark(bookmark.getId());

		// Then
		bot.cTabItem("Comments").activate();
		SWTBotStyledText commentsControl = bot.styledText();
		assertThat(commentsControl.getText()).isEmpty();
		assertThat(UIThreadRunnable.syncExec(() -> commentsControl.widget.getEditable())).isFalse();

		bot.cTabItem("Properties").activate();
		SWTBotTree treeControl = bot.tree();
		assertThat(treeControl.rowCount()).isEqualTo(0);
		
		bot.cTabItem("Snippet").activate();
		SWTBotStyledText snippetControl = bot.styledText();
		assertThat(snippetControl.getText()).isEqualTo("");
		assertThat(UIThreadRunnable.syncExec(() -> snippetControl.widget.getEditable())).isFalse();

	}

}
