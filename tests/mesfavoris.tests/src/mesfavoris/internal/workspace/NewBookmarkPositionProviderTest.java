package mesfavoris.internal.workspace;

import static mesfavoris.MesFavoris.DEFAULT_BOOKMARKFOLDER_ID;
import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmark;
import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmarkFolder;
import static mesfavoris.tests.commons.bookmarks.MainBookmarkDatabaseHelper.addBookmark;
import static mesfavoris.tests.commons.bookmarks.MainBookmarkDatabaseHelper.deleteAllBookmarksExceptDefaultBookmarkFolder;
import static mesfavoris.tests.commons.bookmarks.MainBookmarkDatabaseHelper.deleteBookmark;
import static mesfavoris.tests.commons.bookmarks.MainBookmarkDatabaseHelper.getBookmarksRootFolderId;
import static mesfavoris.tests.commons.ui.SWTBotViewHelper.closeWelcomeView;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import mesfavoris.BookmarksException;
import mesfavoris.MesFavoris;
import mesfavoris.internal.service.operations.utils.INewBookmarkPositionProvider;
import mesfavoris.internal.service.operations.utils.NewBookmarkPosition;
import mesfavoris.internal.service.operations.utils.NewBookmarkPositionProvider;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.tests.commons.ui.BookmarksViewDriver;

public class NewBookmarkPositionProviderTest {
	private SWTWorkbenchBot bot;
	private BookmarksViewDriver bookmarksViewDriver;
	private INewBookmarkPositionProvider newBookmarkPositionProvider;
	private long previousTimeout;
	
	@Before
	public void setUp() throws BookmarksException {
		bot = new SWTWorkbenchBot();
		setSWTBotTimeoutToAtLeast(Duration.ofSeconds(15));
		closeWelcomeView();
		bookmarksViewDriver = new BookmarksViewDriver(bot);
		bookmarksViewDriver.showView();
		newBookmarkPositionProvider = new NewBookmarkPositionProvider(MesFavoris.getBookmarkDatabase());
	}

	@After
	public void tearDown() throws BookmarksException {
		restoreSWTBotTimeout();
		deleteAllBookmarksExceptDefaultBookmarkFolder();
	}

	private void setSWTBotTimeoutToAtLeast(Duration duration) {
		previousTimeout = SWTBotPreferences.TIMEOUT;
		if (previousTimeout < duration.toMillis()) {
			SWTBotPreferences.TIMEOUT = duration.toMillis();
		}
	}
	
	private void restoreSWTBotTimeout() {
		SWTBotPreferences.TIMEOUT = previousTimeout;
	}
	
	@Test
	public void testNewBookmarkPositionWhenNothingIsSelected() {
		// Given
		bookmarksViewDriver.tree().unselect();

		// When
		NewBookmarkPosition position = newBookmarkPositionProvider.getNewBookmarkPosition(getActivePage());

		// Then
		assertThat(position.getParentBookmarkId()).isEqualTo(DEFAULT_BOOKMARKFOLDER_ID);
		assertThat(position.getBookmarkId()).isEmpty();
	}

	@Test
	public void testNewBookmarkPositionWhenFolderIsSelected() throws Exception {
		// Given
		BookmarkFolder bookmarkFolder = bookmarkFolder("myFolder").build();
		addBookmark(getBookmarksRootFolderId(), bookmarkFolder);
		bookmarksViewDriver.tree().select("myFolder");

		// When
		NewBookmarkPosition position = newBookmarkPositionProvider.getNewBookmarkPosition(getActivePage());

		// Then
		assertThat(position.getParentBookmarkId()).isEqualTo(bookmarkFolder.getId());
		assertThat(position.getBookmarkId()).isEmpty();
	}

	@Test
	public void testNewBookmarkPositionWhenBookmarkIsSelected() throws Exception {
		// Given
		BookmarkFolder bookmarkFolder = bookmarkFolder("myFolder").build();
		Bookmark bookmark = bookmark("myBookmark").build();
		addBookmark(getBookmarksRootFolderId(), bookmarkFolder);
		addBookmark(bookmarkFolder.getId(), bookmark);
		bookmarksViewDriver.tree().expandNode("myFolder").select("myBookmark");

		// When
		NewBookmarkPosition position = newBookmarkPositionProvider.getNewBookmarkPosition(getActivePage());

		// Then
		assertThat(position.getParentBookmarkId()).isEqualTo(bookmarkFolder.getId());
		assertThat(position.getBookmarkId()).contains(bookmark.getId());
	}	
	
	@Test
	public void testNewBookmarkPositionWhenDefaultBookmarkFolderDoesNotExist() throws Exception {
		// Given
		deleteBookmark(DEFAULT_BOOKMARKFOLDER_ID);
		bookmarksViewDriver.tree().unselect();

		// When
		NewBookmarkPosition position = newBookmarkPositionProvider.getNewBookmarkPosition(getActivePage());

		// Then
		assertThat(position.getParentBookmarkId()).isEqualTo(DEFAULT_BOOKMARKFOLDER_ID);
		assertThat(position.getBookmarkId()).isEmpty();
	}

	private IWorkbenchPage getActivePage() {
		return UIThreadRunnable.syncExec(() -> PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage());
	}


}
