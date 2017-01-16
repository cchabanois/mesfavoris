package mesfavoris.internal.views;

import static mesfavoris.tests.commons.ui.SWTBotViewHelper.closeWelcomeView;
import static mesfavoris.tests.commons.ui.SWTBotViewHelper.showView;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;

import mesfavoris.BookmarksException;
import mesfavoris.MesFavoris;
import mesfavoris.commons.ui.wizards.datatransfer.BundleProjectImportOperation;
import mesfavoris.internal.workspace.DefaultBookmarkFolderProvider;
import mesfavoris.model.BookmarkId;
import mesfavoris.service.IBookmarksService;

public class BookmarksViewTest {
	private static final String PROJECT_NAME = "BookmarksViewTest";
	private SWTWorkbenchBot bot;
	private IBookmarksService bookmarksService;

	@BeforeClass
	public static void beforeClass() throws Exception {
		importProjectFromTemplate(PROJECT_NAME, "commons-cli");

	}

	@Before
	public void setUp() throws BookmarksException {
		bookmarksService = MesFavoris.getBookmarksService();
		bot = new SWTWorkbenchBot();
		closeWelcomeView();
		showView(BookmarksView.ID);
	}

	@After
	public void tearDown() throws BookmarksException {
		BookmarkId rootFolderId = bookmarksService.getBookmarksTree().getRootFolder().getId();
		bookmarksService.deleteBookmarks(bookmarksService.getBookmarksTree().getChildren(rootFolderId).stream()
				.map(bookmark -> bookmark.getId())
				.filter(bookmarkId -> !bookmarkId.equals(DefaultBookmarkFolderProvider.DEFAULT_BOOKMARKFOLDER_ID))
				.collect(Collectors.toList()), true);
	}

	@Test
	public void testViewHasDefaultFolderAndAllVirtualFolders() {
		// Given
		SWTBotView botView = bot.viewById(BookmarksView.ID);
		assertEquals("Mes Favoris", botView.getTitle());

		// When
		List<SWTBotTreeItem> items = Arrays.asList(botView.bot().tree().getAllItems());

		// Then
		assertThat(items).extracting(item -> item.getText()).containsOnly("default", "Most visited", "Latest visited",
				"Recent bookmarks", "Numbered bookmarks");
	}

	private static void importProjectFromTemplate(String projectName, String templateName)
			throws InvocationTargetException, InterruptedException {
		Bundle bundle = Platform.getBundle("mesfavoris.tests");
		new BundleProjectImportOperation(bundle, projectName, "/projects/" + templateName + "/").run(null);
	}

}
