package mesfavoris.internal.service.operations;

import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmark;
import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmarkFolder;
import static mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder.bookmarksTree;
import static mesfavoris.tests.commons.bookmarks.MainBookmarkDatabaseHelper.deleteAllBookmarksExceptDefaultBookmarkFolder;
import static mesfavoris.tests.commons.ui.SWTBotViewHelper.closeWelcomeView;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import java.util.concurrent.TimeoutException;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import mesfavoris.BookmarksException;
import mesfavoris.MesFavoris;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.service.IBookmarksService;
import mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder;
import mesfavoris.tests.commons.ui.BookmarksViewDriver;
import mesfavoris.tests.commons.waits.Waiter;

/**
 * -Djava.awt.headless=true needed on mac for these tests. See
 * https://github.com/GoogleCloudPlatform/google-cloud-eclipse/issues/611
 * 
 * @author cchabanois
 *
 */
public class ShowInBookmarksViewOperationTest {
	private SWTWorkbenchBot bot;
	private IBookmarksService bookmarksService;
	private BookmarksViewDriver bookmarksViewDriver;

	@Before
	public void setUp() throws BookmarksException {
		bookmarksService = MesFavoris.getBookmarksService();
		bot = new SWTWorkbenchBot();
		closeWelcomeView();
		bookmarksViewDriver = new BookmarksViewDriver(bot);
		bookmarksService.addBookmarksTree(bookmarksService.getBookmarksTree().getRootFolder().getId(),
				createBookmarksTree(), (bookmarksTree) -> {
				});
	}

	@After
	public void tearDown() throws BookmarksException {
		deleteAllBookmarksExceptDefaultBookmarkFolder();
	}

	@Test
	public void testShowInBookmarksView() throws TimeoutException {
		// Given
		IWorkbenchPage activePage = UIThreadRunnable
				.syncExec(() -> PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage());
		assertNotNull(activePage);

		// When
		bookmarksService.showInBookmarksView(activePage, new BookmarkId("bookmark21"), true);

		// Then
		SWTBotTree botTree = bookmarksViewDriver.tree();
		Waiter.waitUntil("bookmark not selected", () -> {
			assertThat(botTree.selection().get(0).get(0)).isEqualTo("bookmark21");
			return true;
		});
	}

	private BookmarksTree createBookmarksTree() {
		BookmarksTreeBuilder bookmarksTreeBuilder = bookmarksTree("folder1");
		bookmarksTreeBuilder.addBookmarks("folder1", bookmarkFolder("folder2"), bookmarkFolder("folder3"));
		bookmarksTreeBuilder.addBookmarks("folder2", bookmarkFolder("folder21"), bookmark("bookmark21"));

		return bookmarksTreeBuilder.build();
	}

}
