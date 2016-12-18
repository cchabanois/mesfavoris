package mesfavoris.java.editor;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;
import static mesfavoris.java.JavaBookmarkProperties.*;
import mesfavoris.commons.ui.wizards.datatransfer.BundleProjectImportOperation;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_LINE_CONTENT;

public class JavaEditorBookmarkPropertiesProviderTest {
	private static final String PROJECT_NAME = "JavaEditorBookmarkPropertiesProviderTest";
	private SWTWorkbenchBot bot;
	private IJavaProject javaProject;
	private JavaEditorBookmarkPropertiesProvider javaEditorBookmarkPropertiesProvider;

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
		javaEditorBookmarkPropertiesProvider = new JavaEditorBookmarkPropertiesProvider();
	}

	@Test
	public void testBookmarkInsideMethod() throws Exception {
		// Given
		SWTBotEclipseEditor textEditor = textEditor("org.apache.commons.cli.BasicParser");
		textEditor.navigateTo(48, 0);
		Map<String, String> bookmarkProperties = new HashMap<>();

		// When
		IWorkbenchPart part = getActivePart();
		ISelection selection = getSelection(part);
		javaEditorBookmarkPropertiesProvider.addBookmarkProperties(bookmarkProperties, part, selection,
				new NullProgressMonitor());

		// Then
		assertThat(bookmarkProperties).containsEntry(PROP_LINE_NUMBER_INSIDE_ELEMENT, "5")
				.containsEntry(PROP_JAVA_DECLARING_TYPE, "org.apache.commons.cli.BasicParser")
				.containsEntry(PROP_JAVA_ELEMENT_NAME, "flatten").containsEntry(PROP_JAVA_ELEMENT_KIND, KIND_METHOD)
				.containsEntry(PROP_LINE_CONTENT, "return arguments;")
				.containsEntry(PROP_JAVA_METHOD_SIGNATURE, "String[] flatten(Options,String[],boolean)");
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
		Bundle bundle = Platform.getBundle("mesfavoris.java.tests");
		new BundleProjectImportOperation(bundle, projectName, "/projects/" + templateName + "/").run(null);
	}

	private SWTBotEclipseEditor textEditor(String fullyQualifiedType) throws JavaModelException, PartInitException {
		IType type = javaProject.findType("org.apache.commons.cli.BasicParser");

		IEditorPart editorPart = UIThreadRunnable.syncExec(new Result<IEditorPart>() {

			public IEditorPart run() {
				try {
					return JavaUI.openInEditor(type, true, true);
				} catch (PartInitException | JavaModelException e) {
					throw new RuntimeException(e);
				}
			}

		});

		// IEditorPart editorPart = JavaUI.openInEditor(type, true, true);
		SWTBotEditor editor = bot.editorById(editorPart.getEditorSite().getId());
		return editor.toTextEditor();
	}

	private void closeWelcomeView() {
		try {
			bot.viewByTitle("Welcome").close();
		} catch (WidgetNotFoundException e) {
		}
	}

}
