package mesfavoris.java;

import static mesfavoris.java.JavaBookmarkProperties.KIND_METHOD;
import static mesfavoris.java.JavaBookmarkProperties.PROP_JAVA_DECLARING_TYPE;
import static mesfavoris.java.JavaBookmarkProperties.PROP_JAVA_ELEMENT_KIND;
import static mesfavoris.java.JavaBookmarkProperties.PROP_JAVA_ELEMENT_NAME;
import static mesfavoris.java.JavaBookmarkProperties.PROP_LINE_NUMBER_INSIDE_ELEMENT;
import static mesfavoris.tests.commons.ui.SWTBotViewHelper.closeWelcomeView;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;

import com.google.common.collect.ImmutableMap;

import mesfavoris.commons.ui.wizards.datatransfer.BundleProjectImportOperation;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;

public class GotoInsideJavaElementBookmarkTest {
	private static final String PROJECT_NAME = "GotoInsideJavaElementBookmarkTest";
	private SWTWorkbenchBot bot;
	private IJavaProject javaProject;
	private GotoInsideJavaElementBookmark gotoBookmark;
	private JavaTypeMemberBookmarkLocationProvider javaBookmarkLocationProvider = new JavaTypeMemberBookmarkLocationProvider();

	
	@BeforeClass
	public static void beforeClass() throws Exception {
		importProjectFromTemplate(PROJECT_NAME, "commons-cli");
		// according to SWTBotEclipseEditor doc "in eclipse editors, folding has
		// no incidence on line numbers." but this does not seem to
		// be true ...
		PreferenceConstants.getPreferenceStore().setValue(PreferenceConstants.EDITOR_FOLDING_ENABLED, false);
	}

	@AfterClass
	public static void afterClass() {
		PreferenceConstants.getPreferenceStore().setValue(PreferenceConstants.EDITOR_FOLDING_ENABLED, true);
	}

	@Before
	public void setUp() {
		bot = new SWTWorkbenchBot();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(PROJECT_NAME);
		javaProject = JavaCore.create(project);
		closeWelcomeView();
		gotoBookmark = new GotoInsideJavaElementBookmark();
	}

	
	@Test
	public void testGotoInsideMethod() {
		// Given
		Bookmark bookmark = new Bookmark(new BookmarkId(),
				ImmutableMap.of(PROP_JAVA_DECLARING_TYPE, "org.apache.commons.cli.DefaultParser",
						PROP_JAVA_ELEMENT_KIND, KIND_METHOD, PROP_JAVA_ELEMENT_NAME, "handleProperties",
						PROP_LINE_NUMBER_INSIDE_ELEMENT, "7"));
		JavaTypeMemberBookmarkLocation location = javaBookmarkLocationProvider.getBookmarkLocation(bookmark,
				new NullProgressMonitor());		

		// When
		boolean result = gotoBookmark(bookmark, location);
		
		// Then
		assertTrue(result);
		SWTBotEclipseEditor textEditor = bot.activeEditor().toTextEditor();
		assertEquals("DefaultParser.java", textEditor.getTitle());
		assertEquals(146, textEditor.cursorPosition().line);
	}
	
	private boolean gotoBookmark(Bookmark bookmark, JavaTypeMemberBookmarkLocation location) {
		return UIThreadRunnable
				.syncExec(() -> gotoBookmark.gotoBookmark(getActiveWindow(), bookmark, location));
	}
	
	private IWorkbenchWindow getActiveWindow() {
		return UIThreadRunnable
				.syncExec(() -> PlatformUI.getWorkbench().getActiveWorkbenchWindow());
	}	
	
	private static void importProjectFromTemplate(String projectName, String templateName)
			throws InvocationTargetException, InterruptedException {
		Bundle bundle = Platform.getBundle("mesfavoris.java.tests");
		new BundleProjectImportOperation(bundle, projectName, "/projects/" + templateName + "/").run(null);
	}	
	
}
