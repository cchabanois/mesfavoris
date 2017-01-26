package mesfavoris.texteditor.internal;

import static mesfavoris.tests.commons.ui.SWTBotViewHelper.closeWelcomeView;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;

import mesfavoris.commons.ui.wizards.datatransfer.BundleProjectImportOperation;

public class TextEditorBookmarkPropertiesProviderTest {
	private static final String PROJECT_NAME = "TextEditorBookmarkPropertiesProviderTest";
	private SWTWorkbenchBot bot;
	private TextEditorBookmarkPropertiesProvider textEditorBookmarkPropertiesProvider;
	private IProject project;

	@BeforeClass
	public static void beforeClass() throws Exception {
		importProjectFromTemplate(PROJECT_NAME, "commons-cli");
	}

	@Before
	public void setUp() {
		bot = new SWTWorkbenchBot();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		project = root.getProject(PROJECT_NAME);
		closeWelcomeView();
		textEditorBookmarkPropertiesProvider = new TextEditorBookmarkPropertiesProvider();
	}

	@Test
	public void testBookmarkInsideTextFile() throws Exception {
		// Given
		SWTBotEclipseEditor textEditor = textEditor("LICENSE.txt");
		textEditor.navigateTo(25, 0);
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
				.containsEntry(PROPERTY_NAME, "LICENSE.txt:26")
				.containsEntry(PROP_WORKSPACE_PATH, "/TextEditorBookmarkPropertiesProviderTest/LICENSE.txt")
				.containsKey(PROP_FILE_PATH);
	}

	private SWTBotEclipseEditor textEditor(String fileName) throws PartInitException {
		IFile file = project.getFile(fileName);

		IEditorPart editorPart = UIThreadRunnable.syncExec(new Result<IEditorPart>() {

			public IEditorPart run() {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IEditorPart editorPart = IDE.openEditor(page, file);
					return editorPart;
				} catch (PartInitException e) {
					return null;
				}
			}

		});
		SWTBotEditor editor = bot.editorById(editorPart.getEditorSite().getId());
		return editor.toTextEditor();
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
