package mesfavoris.internal.handlers;

import static mesfavoris.tests.commons.ui.SWTBotViewHelper.closeWelcomeView;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;

import mesfavoris.BookmarksException;
import mesfavoris.commons.ui.wizards.datatransfer.BundleProjectImportOperation;
import mesfavoris.tests.commons.ui.BookmarksViewDriver;

public class AddBookmarkHandlerTest {
	private static final String PROJECT_NAME = "AddBookmarkHandlerTest";
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
	public void testAddBookmarkHandler() {
		
	}
	
	private static void importProjectFromTemplate(String projectName, String templateName)
			throws InvocationTargetException, InterruptedException {
		Bundle bundle = Platform.getBundle("mesfavoris.tests");
		new BundleProjectImportOperation(bundle, projectName, "/projects/" + templateName + "/").run(null);
	}	
	
}
