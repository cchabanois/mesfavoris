package mesfavoris.internal.views;

import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmark;
import static mesfavoris.tests.commons.ui.SWTBotViewHelper.closeWelcomeView;
import static mesfavoris.tests.commons.waits.Waiter.waitUntil;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_LINE_CONTENT;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_WORKSPACE_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;

import com.google.common.collect.ImmutableMap;

import mesfavoris.BookmarksException;
import mesfavoris.MesFavoris;
import mesfavoris.commons.ui.wizards.datatransfer.BundleProjectImportOperation;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.tests.commons.ui.BookmarksViewDriver;
import mesfavoris.tests.commons.waits.Waiter;
import mesfavoris.texteditor.TextEditorBookmarkProperties;

@RunWith(SWTBotJunit4ClassRunner.class)
public class BookmarksViewTest {
	private static final String PROJECT_NAME = "BookmarksViewTest";
	private SWTWorkbenchBot bot;
	private BookmarksViewDriver bookmarksViewDriver;

	@BeforeClass
	public static void beforeClass() throws Exception {
		importProjectFromTemplate(PROJECT_NAME, "commons-cli");
	}

	@Before
	public void setUp() throws BookmarksException {
		bot = new SWTWorkbenchBot();
		closeWelcomeView();
		bookmarksViewDriver = new BookmarksViewDriver(bot);
		bookmarksViewDriver.showView();
	}

	@After
	public void tearDown() throws BookmarksException {
		bookmarksViewDriver.deleteAllBookmarksExceptDefaultBookmarkFolder();
	}

	@Test
	public void testViewHasDefaultFolderAndAllVirtualFolders() {
		// Given
		SWTBotView botView = bookmarksViewDriver.view();
		assertEquals("Mes Favoris", botView.getTitle());

		// When
		List<SWTBotTreeItem> items = Arrays.asList(bookmarksViewDriver.tree().getAllItems());

		// Then
		assertThat(items).extracting(item -> item.getText()).containsOnly("default", "Most visited", "Latest visited",
				"Recent bookmarks", "Numbered bookmarks");
	}

	@Test
	public void testViewUpdatedWhenBookmarkIsAdded() throws Exception {
		// Given
		Bookmark bookmark = bookmark("bookmark").withProperty(Bookmark.PROPERTY_NAME, "bookmark").build();

		// When
		addBookmark(getBookmarksRootFolderId(), bookmark);

		// Then
		waitUntil("Cannot find new bookmark", () -> bookmarksViewDriver.tree().getTreeItem("bookmark"));
	}

	@Test
	public void testGotoBookmarkOnDoubleClick() throws Exception {
		// Given
		Bookmark bookmark = new Bookmark(new BookmarkId(), ImmutableMap.of(Bookmark.PROPERTY_NAME, "bookmark",
				PROP_WORKSPACE_PATH, "/BookmarksViewTest/src/main/java/org/apache/commons/cli/DefaultParser.java",
				PROP_LINE_CONTENT,
				"for (Enumeration<?> enumeration = properties.propertyNames(); enumeration.hasMoreElements();)"));
		addBookmark(getBookmarksRootFolderId(), bookmark);
		SWTBotTreeItem bookmarkTreeItem = waitUntil("Cannot find new bookmark",
				() -> bookmarksViewDriver.tree().getTreeItem("bookmark"));

		// When
		bookmarkTreeItem.doubleClick();

		// Then
		waitUntil("cannot go to bookmark", () -> "DefaultParser.java".equals(getActivePart().getTitle()));
		IWorkbenchPart workbenchPart = getActivePart();
		ITextSelection selection = (ITextSelection) getSelection(workbenchPart);
		assertEquals("DefaultParser.java", workbenchPart.getTitle());
		assertEquals(146, selection.getStartLine());
	}

	@Test
	public void testBookmarkProblemAddedOnDoubleClick() throws Exception {
		// Given
		Bookmark bookmark = new Bookmark(new BookmarkId(), ImmutableMap.of(Bookmark.PROPERTY_NAME, "bookmark",
				PROP_WORKSPACE_PATH, "/BookmarksViewTest/src/main/java/org/apache/commons/cli/DefaultParser.java",
				PROP_LINE_CONTENT,
				"for (Enumeration<?> enumeration = properties.propertyNames(); enumeration.hasMoreElements();)"));
		addBookmark(getBookmarksRootFolderId(), bookmark);
		SWTBotTreeItem bookmarkTreeItem = waitUntil("Cannot find new bookmark",
				() -> bookmarksViewDriver.tree().getTreeItem("bookmark"));

		// When
		bookmarkTreeItem.doubleClick();

		// Then
		waitUntil("There should be a bookmark problem",
				() -> "One bookmark problem detected".equals(bookmarksViewDriver.form().getMessage()));
	}

	@Test
	public void testUpdateBookmarkPropertiesAction() throws Exception {
		// Given
		BookmarkId bookmarkId = new BookmarkId();
		Bookmark bookmark = new Bookmark(bookmarkId, ImmutableMap.of(Bookmark.PROPERTY_NAME, "bookmark",
				PROP_WORKSPACE_PATH, "/BookmarksViewTest/src/main/java/org/apache/commons/cli/DefaultParser.java",
				PROP_LINE_CONTENT,
				"for (Enumeration<?> enumeration = properties.propertyNames(); enumeration.hasMoreElements();)"));
		addBookmark(getBookmarksRootFolderId(), bookmark);
		SWTBotTreeItem bookmarkTreeItem = Waiter.waitUntil("Cannot find new bookmark",
				() -> bookmarksViewDriver.tree().getTreeItem("bookmark"));
		bookmarkTreeItem.doubleClick();
		waitUntil("There should be a bookmark problem",
				() -> "One bookmark problem detected".equals(bookmarksViewDriver.form().getMessage()));

		// When
		bookmarksViewDriver.form().toolbarButtonWithTooltip("Use new properties", 0).click();

		// Then
		waitUntil("There should be no bookmark problem", () -> bookmarksViewDriver.form().getMessage() == null);
		assertEquals("146", getBookmarksTree().getBookmark(bookmarkId)
				.getPropertyValue(TextEditorBookmarkProperties.PROP_LINE_NUMBER));
	}

	private static void importProjectFromTemplate(String projectName, String templateName)
			throws InvocationTargetException, InterruptedException {
		Bundle bundle = Platform.getBundle("mesfavoris.tests");
		new BundleProjectImportOperation(bundle, projectName, "/projects/" + templateName + "/").run(null);
	}

	private void addBookmark(BookmarkId parentId, Bookmark... bookmark) throws BookmarksException {
		MesFavoris.getBookmarkDatabase()
				.modify(bookmarksTreeModifier -> bookmarksTreeModifier.addBookmarks(parentId, Arrays.asList(bookmark)));
	}

	private IWorkbenchPart getActivePart() {
		return UIThreadRunnable
				.syncExec(() -> PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().getActivePart());
	}

	private ISelection getSelection(IWorkbenchPart part) {
		return UIThreadRunnable.syncExec(() -> part.getSite().getSelectionProvider().getSelection());
	}

	private BookmarkId getBookmarksRootFolderId() {
		return getBookmarksTree().getRootFolder().getId();
	}

	private BookmarksTree getBookmarksTree() {
		return MesFavoris.getBookmarkDatabase().getBookmarksTree();
	}

}
