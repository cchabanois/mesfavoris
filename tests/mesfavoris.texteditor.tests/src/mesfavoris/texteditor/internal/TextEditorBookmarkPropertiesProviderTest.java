package mesfavoris.texteditor.internal;

import static mesfavoris.tests.commons.ui.SWTBotEditorHelper.closeAllEditors;
import static mesfavoris.tests.commons.ui.SWTBotEditorHelper.textEditor;
import static mesfavoris.tests.commons.ui.SWTBotViewHelper.closeWelcomeView;
import static mesfavoris.tests.commons.waits.Waiter.waitUntil;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROPERTY_NAME;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_FILE_PATH;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_LINE_CONTENT;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_LINE_NUMBER;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_PROJECT_NAME;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_WORKSPACE_PATH;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;

import mesfavoris.commons.ui.wizards.datatransfer.BundleProjectImportOperation;

public class TextEditorBookmarkPropertiesProviderTest {
	private static final String PROJECT_NAME = "TextEditorBookmarkPropertiesProviderTest";
	private TextEditorBookmarkPropertiesProvider textEditorBookmarkPropertiesProvider;
	private IProject project;

	@BeforeClass
	public static void beforeClass() throws Exception {
		importProjectFromTemplate(PROJECT_NAME, "commons-cli");
	}

	@Before
	public void setUp() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		project = root.getProject(PROJECT_NAME);
		closeWelcomeView();
		closeAllEditors();
		textEditorBookmarkPropertiesProvider = new TextEditorBookmarkPropertiesProvider();
	}

	@Test
	public void testBookmarkInsideTextFile() throws Exception {
		// Given
		SWTBotEclipseEditor textEditor = textEditor(project, "LICENSE.txt");
		textEditor.navigateTo(25, 0);
		waitUntil("cursor is not on line 25", () -> textEditor.cursorPosition().line == 25);
		Map<String, String> bookmarkProperties = new HashMap<>();

		// When
		IWorkbenchPart part = getActivePart();
		ISelection selection = getSelection(part);
		textEditorBookmarkPropertiesProvider.addBookmarkProperties(bookmarkProperties, part, selection,
				new NullProgressMonitor());

		// Then
		assertThat(bookmarkProperties).containsEntry(PROP_LINE_NUMBER, "25")
				.containsEntry(PROP_PROJECT_NAME, PROJECT_NAME)
				.containsEntry(PROP_LINE_CONTENT,
						"\"Source\" form shall mean the preferred form for making modifications,")
				.containsEntry(PROPERTY_NAME, "LICENSE.txt : \"Source\" form shall mean the preferred form for making modifications,")
				.containsEntry(PROP_WORKSPACE_PATH, "/TextEditorBookmarkPropertiesProviderTest/LICENSE.txt")
				.containsKey(PROP_FILE_PATH);
	}

	private static void importProjectFromTemplate(String projectName, String templateName)
			throws InvocationTargetException, InterruptedException {
		Bundle bundle = Platform.getBundle("mesfavoris.texteditor.tests");
		new BundleProjectImportOperation(bundle, projectName, "/projects/" + templateName + "/").run(null);
	}

	private ISelection getSelection(IWorkbenchPart part) {
		return UIThreadRunnable.syncExec(() -> part.getSite().getSelectionProvider().getSelection());
	}

	private IWorkbenchPart getActivePart() {
		return UIThreadRunnable
				.syncExec(() -> PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().getActivePart());
	}

}
