package mesfavoris.internal.views.properties;

import static mesfavoris.MesFavoris.getBookmarkProblems;
import static mesfavoris.model.Bookmark.PROPERTY_COMMENT;
import static mesfavoris.model.Bookmark.PROPERTY_NAME;
import static mesfavoris.tests.commons.bookmarks.MainBookmarkDatabaseHelper.addBookmark;
import static mesfavoris.tests.commons.bookmarks.MainBookmarkDatabaseHelper.getBookmarksRootFolderId;
import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import mesfavoris.MesFavoris;
import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.views.details.BookmarkDetailsPart;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;
import mesfavoris.problems.BookmarkProblem;
import mesfavoris.tests.commons.ui.AbstractControlTest;
import mesfavoris.tests.commons.waits.Waiter;

public class PropertiesBookmarkDetailPartTest extends AbstractControlTest {
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
	public void testPropertiesUpdatedWhenBookmarkIsModified() throws Exception {
		// Given
		Bookmark bookmark = new Bookmark(new BookmarkId(),
				ImmutableMap.of(PROPERTY_NAME, "my bookmark", PROPERTY_COMMENT, "my comment"));
		addBookmark(getBookmarksRootFolderId(), bookmark);
		setBookmark(bookmark);
		bot.cTabItem("Properties").activate();
		SWTBotTreeItem treeItem = bot.tree().expandNode("default");
		assertThat(treeItem.getNode(PROPERTY_COMMENT).cell(1)).isEqualTo("my comment");

		// When
		MesFavoris.getBookmarksService().setComment(bookmark.getId(), "my comment modified");

		// Then
		Waiter.waitUntil("Property not modified", () -> {
			return treeItem.getNode(PROPERTY_COMMENT).cell(1).equals("my comment modified");
		});
	}

	@Test
	public void testPropertiesUpdatedWhenBookmarkProblemChanges() throws Exception {
		// Given
		Bookmark bookmark = new Bookmark(new BookmarkId(),
				ImmutableMap.of(PROPERTY_NAME, "my bookmark", PROPERTY_COMMENT, "my comment"));
		addBookmark(getBookmarksRootFolderId(), bookmark);
		setBookmark(bookmark);
		bot.cTabItem("Properties").activate();

		// When
		getBookmarkProblems().add(new BookmarkProblem(bookmark.getId(), BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE,
				ImmutableMap.of(PROPERTY_COMMENT, "my comment updated")));

		// Then
		SWTBotTreeItem treeItem = bot.tree().expandNode("default").expandNode("comment");
		Waiter.waitUntil("Problem not added to properties", () -> {
			treeItem.expand();
			return treeItem.getNode("Updated value").cell(1).equals("my comment updated");
		});
	}

	private void setBookmark(Bookmark bookmark) {
		UIThreadRunnable.asyncExec(() -> bookmarkDetailsPart.setBookmark(bookmark));
	}
}
