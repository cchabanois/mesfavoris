package mesfavoris.internal.placeholders.usage;

import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmark;
import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmarkFolder;
import static mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder.bookmarksTree;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_FILE_PATH;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_FOLDER_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import mesfavoris.internal.placeholders.PathPlaceholdersMap;
import mesfavoris.internal.remote.InMemoryRemoteBookmarksStore;
import mesfavoris.internal.validation.BookmarksModificationValidator;
import mesfavoris.internal.workspace.DefaultBookmarkFolderProvider;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.placeholders.PathPlaceholder;
import mesfavoris.remote.RemoteBookmarksStoreManager;
import mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder;
import mesfavoris.tests.commons.ui.AbstractDialogTest;

public class PathPlaceholderUsageDialogTest extends AbstractDialogTest {
	private PathPlaceholderUsageDialog dialog;
	private BookmarkDatabase bookmarkDatabase;
	private InMemoryRemoteBookmarksStore remoteBookmarksStore;
	private RemoteBookmarksStoreManager remoteBookmarksStoreManager;
	private PathPlaceholdersMap pathPlaceholders = new PathPlaceholdersMap();
	private final List<String> pathPropertyNames = Lists.newArrayList(PROP_FILE_PATH, PROP_FOLDER_PATH);

	@Before
	public void setUp() throws Exception {
		IEventBroker eventBroker = mock(IEventBroker.class);
		this.remoteBookmarksStore = new InMemoryRemoteBookmarksStore(eventBroker);
		this.remoteBookmarksStoreManager = new RemoteBookmarksStoreManager(
				() -> Lists.newArrayList(remoteBookmarksStore));
		BookmarksModificationValidator bookmarksModificationValidator = new BookmarksModificationValidator(
				remoteBookmarksStoreManager);
		this.bookmarkDatabase = new BookmarkDatabase("testId", createBookmarksTree(), bookmarksModificationValidator);
		pathPlaceholders.add(new PathPlaceholder("MY_PROJECT", Path.forPosix("/home/cchabanois/myProject")));
		addToRemoteBookmarksStore(new BookmarkId("folder2"));
	}

	@Test
	public void testDisplayCollapsableAndCollapsedPaths() {
		// Given

		// When
		openDialog(shell -> createDialog(shell, "MY_PROJECT"));

		// Then
		assertBookmarksWithCollapsablePath("bookmark3", "bookmark6");
		assertBookmarksWithCollapsedPath("bookmark1", "bookmark2", "bookmark5", "bookmark7");
	}

	@Test
	public void testCollapseAllWhenNotConnected() {
		// Given
		openDialog(shell -> createDialog(shell, "MY_PROJECT"));

		// When
		collapseAll();

		// Then
		assertBookmarksWithCollapsablePath("bookmark6");
		assertBookmarksWithCollapsedPath("bookmark1", "bookmark2", "bookmark3", "bookmark5", "bookmark7");
	}

	@Test
	public void testCollapseAllWhenConnected() throws Exception {
		// Given
		remoteBookmarksStore.connect(new NullProgressMonitor());
		openDialog(shell -> createDialog(shell, "MY_PROJECT"));

		// When
		collapseAll();

		// Then
		assertBookmarksWithCollapsablePath();
		assertBookmarksWithCollapsedPath("bookmark1", "bookmark2", "bookmark3", "bookmark5", "bookmark6", "bookmark7");
	}
	
	@Test
	public void testExpandAllWhenNotConnected() {
		// Given
		openDialog(shell -> createDialog(shell, "MY_PROJECT"));

		// When
		expandAll();

		// Then
		assertBookmarksWithCollapsablePath("bookmark1", "bookmark2", "bookmark3", "bookmark6");
		assertBookmarksWithCollapsedPath("bookmark5", "bookmark7");
	}

	@Test
	public void testExpandAllWhenConnected() throws Exception {
		// Given
		remoteBookmarksStore.connect(new NullProgressMonitor());
		openDialog(shell -> createDialog(shell, "MY_PROJECT"));

		// When
		expandAll();

		// Then
		assertBookmarksWithCollapsablePath("bookmark1", "bookmark2", "bookmark3", "bookmark5", "bookmark6", "bookmark7");
		assertBookmarksWithCollapsedPath();
	}	
	
	private PathPlaceholderUsageDialog createDialog(Shell shell, String pathPlaceholderName) {
		dialog = new PathPlaceholderUsageDialog(shell, bookmarkDatabase, remoteBookmarksStoreManager, pathPlaceholders,
				pathPlaceholderName, pathPropertyNames);
		return dialog;
	}

	private BookmarksTree createBookmarksTree() {
		BookmarksTreeBuilder bookmarksTreeBuilder = bookmarksTree("root");
		bookmarksTreeBuilder.addBookmarks("root", bookmarkFolder("folder1"), bookmarkFolder("folder2"),
				bookmarkFolder(DefaultBookmarkFolderProvider.DEFAULT_BOOKMARKFOLDER_ID, "default"));
		bookmarksTreeBuilder.addBookmarks("folder1",
				bookmark("bookmark1").withProperty(PROP_FILE_PATH, "${MY_PROJECT}/file1.txt"),
				bookmark("bookmark2").withProperty(PROP_FILE_PATH, "${MY_PROJECT}/file2.txt"),
				bookmark("bookmark3").withProperty(PROP_FILE_PATH, "/home/cchabanois/myProject/file3.txt"),
				bookmark("bookmark4").withProperty(PROP_FILE_PATH, "/home/cchabanois/file4.txt"));
		bookmarksTreeBuilder.addBookmarks("folder2",
				bookmark("bookmark5").withProperty(PROP_FILE_PATH, "${MY_PROJECT}/file5.txt"),
				bookmark("bookmark6").withProperty(PROP_FILE_PATH, "/home/cchabanois/myProject/file6.txt"),
				bookmark("bookmark7").withProperty(PROP_FILE_PATH, "${MY_PROJECT}/file7.txt"));

		return bookmarksTreeBuilder.build();
	}

	private void assertBookmarksWithCollapsablePath(String... bookmarks) {
		SWTBotTable botTable = collapsableBookmarksTable();
		assertBookmarksInTable(botTable, bookmarks);
	}

	private void assertBookmarksWithCollapsedPath(String... bookmarks) {
		SWTBotTable botTable = collapsedBookmarksTable();
		assertBookmarksInTable(botTable, bookmarks);
	}

	private void assertBookmarksInTable(SWTBotTable botTable, String... bookmarks) {
		assertEquals(bookmarks.length, botTable.rowCount());
		for (int i = 0; i < botTable.rowCount(); i++) {
			assertThat(botTable.getTableItem(i).getText()).startsWith(bookmarks[i] + " - ");
		}
	}

	private SWTBotTable collapsableBookmarksTable() {
		return bot.table(0);
	}

	private SWTBotTable collapsedBookmarksTable() {
		return bot.table(1);
	}

	private void addToRemoteBookmarksStore(BookmarkId bookmarkFolderId) throws IOException {
		remoteBookmarksStore.add(bookmarkDatabase.getBookmarksTree().subTree(bookmarkFolderId), bookmarkFolderId,
				new NullProgressMonitor());
	}

	private void collapseAll() {
		bot.button(1).click();
	}

	private void expandAll() {
		bot.button(3).click();
	}
	
}
