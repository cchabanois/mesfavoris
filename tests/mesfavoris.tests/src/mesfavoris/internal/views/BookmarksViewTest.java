package mesfavoris.internal.views;

import static mesfavoris.tests.commons.ui.SWTBotViewHelper.closeWelcomeView;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

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
import mesfavoris.commons.ui.wizards.datatransfer.BundleProjectImportOperation;
import mesfavoris.tests.commons.ui.BookmarksViewDriver;

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

	private static void importProjectFromTemplate(String projectName, String templateName)
			throws InvocationTargetException, InterruptedException {
		Bundle bundle = Platform.getBundle("mesfavoris.tests");
		new BundleProjectImportOperation(bundle, projectName, "/projects/" + templateName + "/").run(null);
	}

}
