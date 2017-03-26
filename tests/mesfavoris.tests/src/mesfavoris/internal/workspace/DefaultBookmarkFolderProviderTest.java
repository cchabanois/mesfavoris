package mesfavoris.internal.workspace;

import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmarkFolder;
import static mesfavoris.tests.commons.ui.SWTBotViewHelper.closeWelcomeView;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import mesfavoris.BookmarksException;
import mesfavoris.MesFavoris;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.tests.commons.ui.BookmarksViewDriver;

public class DefaultBookmarkFolderProviderTest {
	private SWTWorkbenchBot bot;
	private BookmarksViewDriver bookmarksViewDriver;
	private DefaultBookmarkFolderProvider defaultBookmarkFolderProvider;

	@Before
	public void setUp() throws BookmarksException {
		bot = new SWTWorkbenchBot();
		closeWelcomeView();
		bookmarksViewDriver = new BookmarksViewDriver(bot);
		bookmarksViewDriver.showView();
		defaultBookmarkFolderProvider = new DefaultBookmarkFolderProvider(MesFavoris.getBookmarkDatabase());
	}

	@After
	public void tearDown() throws BookmarksException {
		bookmarksViewDriver.deleteAllBookmarksExceptDefaultBookmarkFolder();
	}

	@Test
	public void testDefaultBookmarkWhenNothingIsSelected() {
		// Given
		bookmarksViewDriver.tree().unselect();

		// When
		BookmarkId bookmarkId = defaultBookmarkFolderProvider.getDefaultBookmarkFolder(getActivePage());

		// Then
		assertThat(bookmarkId).isEqualTo(DefaultBookmarkFolderProvider.DEFAULT_BOOKMARKFOLDER_ID);
	}

	@Test
	public void testDefaultBookmarkWhenFolderIsSelected() throws Exception {
		// Given
		BookmarkFolder bookmarkFolder = bookmarkFolder("myFolder").build();
		addBookmark(getBookmarksRootFolderId(), bookmarkFolder);
		bookmarksViewDriver.tree().select("myFolder");

		// When
		BookmarkId bookmarkId = defaultBookmarkFolderProvider.getDefaultBookmarkFolder(getActivePage());

		// Then
		assertThat(bookmarkId).isEqualTo(bookmarkFolder.getId());
	}

	@Test
	public void testDefaultBookmarkWhenDefaultBookmarkFolderDoesNotExist() throws Exception {
		// Given
		deleteBookmark(DefaultBookmarkFolderProvider.DEFAULT_BOOKMARKFOLDER_ID);
		bookmarksViewDriver.tree().unselect();

		// When
		BookmarkId bookmarkId = defaultBookmarkFolderProvider.getDefaultBookmarkFolder(getActivePage());

		// Then
		assertThat(bookmarkId).isEqualTo(DefaultBookmarkFolderProvider.DEFAULT_BOOKMARKFOLDER_ID);
	}

	private IWorkbenchPage getActivePage() {
		return UIThreadRunnable.syncExec(() -> PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage());
	}

	private void addBookmark(BookmarkId parentId, Bookmark... bookmark) throws BookmarksException {
		MesFavoris.getBookmarkDatabase()
				.modify(bookmarksTreeModifier -> bookmarksTreeModifier.addBookmarks(parentId, Arrays.asList(bookmark)));
	}

	private void deleteBookmark(BookmarkId bookmarkId) throws BookmarksException {
		MesFavoris.getBookmarkDatabase()
				.modify(bookmarksTreeModifier -> bookmarksTreeModifier.deleteBookmark(bookmarkId, true));

	}

	private BookmarkId getBookmarksRootFolderId() {
		return getBookmarksTree().getRootFolder().getId();
	}

	private BookmarksTree getBookmarksTree() {
		return MesFavoris.getBookmarkDatabase().getBookmarksTree();
	}

}
