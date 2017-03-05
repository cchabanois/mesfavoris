package mesfavoris.internal.service.operations;

import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmark;
import static mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder.bookmarksTree;
import static mesfavoris.tests.commons.ui.SWTBotViewHelper.closeWelcomeView;
import static mesfavoris.tests.commons.waits.Waiter.waitUntil;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_LINE_NUMBER;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_WORKSPACE_PATH;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.osgi.framework.Bundle;

import com.google.common.collect.Lists;

import mesfavoris.BookmarksException;
import mesfavoris.bookmarktype.IBookmarkLocationProvider;
import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.bookmarktype.IBookmarkPropertyDescriptors;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.bookmarktype.NonUpdatablePropertiesProvider;
import mesfavoris.bookmarktype.PathPropertiesProvider;
import mesfavoris.commons.ui.wizards.datatransfer.BundleProjectImportOperation;
import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.bookmarktypes.extension.PluginBookmarkMarkerAttributesProvider;
import mesfavoris.internal.bookmarktypes.extension.PluginBookmarkTypes;
import mesfavoris.internal.markers.BookmarksMarkers;
import mesfavoris.internal.placeholders.PathPlaceholderResolver;
import mesfavoris.internal.problems.BookmarkProblemsDatabase;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder;

public class GotoBookmarkOperationTest {
	private GotoBookmarkOperation gotoBookmarkOperation;
	private BookmarkDatabase bookmarkDatabase;
	private BookmarkProblemsDatabase bookmarkProblemsDatabase;
	private BookmarksMarkers bookmarksMarkers;

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@BeforeClass
	public static void beforeClass() throws Exception {
		importProjectFromTemplate("gotoBookmarkOperationTest", "commons-cli");
	}

	@Before
	public void setUp() throws IOException {
		IEventBroker eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);
		this.bookmarkDatabase = new BookmarkDatabase("main", getInitialTree());
		this.bookmarkProblemsDatabase = new BookmarkProblemsDatabase(eventBroker, bookmarkDatabase,
				temporaryFolder.newFile());
		this.bookmarkProblemsDatabase.init();
		PluginBookmarkTypes pluginBookmarkTypes = new PluginBookmarkTypes();
		this.bookmarksMarkers = new BookmarksMarkers(bookmarkDatabase,
				new PluginBookmarkMarkerAttributesProvider(pluginBookmarkTypes));
		this.bookmarksMarkers.init();
		IBookmarkLocationProvider bookmarkLocationProvider = BookmarksPlugin.getDefault().getBookmarkLocationProvider();
		IGotoBookmark gotoBookmark = BookmarksPlugin.getDefault().getGotoBookmark();
		IBookmarkPropertiesProvider bookmarkPropertiesProvider = BookmarksPlugin.getDefault()
				.getBookmarkPropertiesProvider();
		IBookmarkPropertyDescriptors bookmarkPropertyDescriptors = BookmarksPlugin.getDefault()
				.getBookmarkPropertyDescriptors();
		CheckBookmarkPropertiesOperation checkBookmarkPropertiesOperation = new CheckBookmarkPropertiesOperation(
				bookmarkDatabase, new NonUpdatablePropertiesProvider(bookmarkPropertyDescriptors),
				new PathPropertiesProvider(bookmarkPropertyDescriptors), bookmarkPropertiesProvider,
				new PathPlaceholderResolver(BookmarksPlugin.getDefault().getPathPlaceholdersStore()),
				bookmarkProblemsDatabase);
		gotoBookmarkOperation = new GotoBookmarkOperation(bookmarkDatabase, bookmarkLocationProvider, gotoBookmark,
				bookmarksMarkers, bookmarkPropertiesProvider, checkBookmarkPropertiesOperation,
				bookmarkProblemsDatabase, eventBroker);
		closeWelcomeView();
	}

	@After
	public void tearDown() throws InterruptedException {
		bookmarkProblemsDatabase.close();
		bookmarksMarkers.close();
	}

	@Test
	public void testGotoBookmark() throws Exception {
		// Given
		Bookmark bookmark = bookmark("LICENSE.txt")
				.withProperty(PROP_WORKSPACE_PATH, "/gotoBookmarkOperationTest/LICENSE.txt")
				.withProperty(PROP_LINE_NUMBER, "10").build();
		addBookmark(new BookmarkId("rootFolder"), bookmark);

		// When
		gotoBookmarkOperation.gotoBookmark(bookmark.getId(), new NullProgressMonitor());
		
		// Then
		IWorkbenchPart workbenchPart = getActivePart();
		ITextSelection selection = (ITextSelection)getSelection(workbenchPart);
		assertEquals("LICENSE.txt", workbenchPart.getTitle());
		assertEquals(10, selection.getStartLine());
		waitUntil("Cannot find marker", () -> bookmarksMarkers.findMarker(bookmark.getId(), null));
	}

	private BookmarksTree getInitialTree() {
		BookmarksTreeBuilder bookmarksTreeBuilder = bookmarksTree("rootFolder");
		return bookmarksTreeBuilder.build();
	}

	private void addBookmark(BookmarkId parentId, Bookmark bookmark) throws BookmarksException {
		bookmarkDatabase.modify(
				bookmarksTreeModifier -> bookmarksTreeModifier.addBookmarks(parentId, Lists.newArrayList(bookmark)));
	}

	private ISelection getSelection(IWorkbenchPart part) {
		return UIThreadRunnable.syncExec(() -> part.getSite().getSelectionProvider().getSelection());
	}

	private IWorkbenchPart getActivePart() {
		return UIThreadRunnable
				.syncExec(() -> PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().getActivePart());
	}	
	
	private static void importProjectFromTemplate(String projectName, String templateName)
			throws InvocationTargetException, InterruptedException {
		Bundle bundle = Platform.getBundle("mesfavoris.tests");
		new BundleProjectImportOperation(bundle, projectName, "/projects/" + templateName + "/").run(null);
	}
}
