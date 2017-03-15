package mesfavoris.texteditor;

import static mesfavoris.tests.commons.ui.SWTBotEditorHelper.closeAllEditors;
import static mesfavoris.tests.commons.ui.SWTBotEditorHelper.textEditor;
import static mesfavoris.tests.commons.ui.SWTBotViewHelper.closeWelcomeView;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;

import mesfavoris.commons.ui.wizards.datatransfer.BundleProjectImportOperation;

public class TextEditorUtilsTest {

	private static final String PROJECT_NAME = "TextEditorUtilsTest";
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
	}

	@Test
	public void testGetLineContent() throws Exception {
		// Given
		textEditor(project, "LICENSE.txt");

		// When
		ITextEditor textEditor = (ITextEditor) getActivePart();
		String lineContent = TextEditorUtils.getLineContent(textEditor, 8);

		// Then
		assertThat(lineContent)
				.isEqualTo("      \"License\" shall mean the terms and conditions for use, reproduction,");
	}

	@Test
	public void testOffsetOfFirstNonWhitespaceCharAtLine() throws Exception {
		// Given
		SWTBotEclipseEditor botTextEditor = textEditor(project, "LICENSE.txt");
		botTextEditor.navigateTo(8, 0);

		// When
		ITextEditor textEditor = (ITextEditor) getActivePart();
		int offset = TextEditorUtils.getOffsetOfFirstNonWhitespaceCharAtLine(textEditor, 8);

		// Then
		ITextSelection textSelection = (ITextSelection) getSelection(textEditor);
		assertThat(offset).isEqualTo(textSelection.getOffset() + "      ".length());
	}

	@Test
	public void testGotoLine() throws Exception {
		// Given
		textEditor(project, "LICENSE.txt");

		// When
		ITextEditor textEditor = (ITextEditor) getActivePart();
		UIThreadRunnable.syncExec(() -> {
			try {
				TextEditorUtils.gotoLine(textEditor, 16);
			} catch (BadLocationException e) {
				fail();
			}
		});

		// Then
		ITextSelection textSelection = (ITextSelection) getSelection(textEditor);
		assertThat(textSelection.getStartLine()).isEqualTo(16);
	}

	private static void importProjectFromTemplate(String projectName, String templateName)
			throws InvocationTargetException, InterruptedException {
		Bundle bundle = Platform.getBundle("mesfavoris.texteditor.tests");
		new BundleProjectImportOperation(bundle, projectName, "/projects/" + templateName + "/").run(null);
	}

	private IWorkbenchPart getActivePart() {
		return UIThreadRunnable
				.syncExec(() -> PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().getActivePart());
	}

	private ISelection getSelection(IWorkbenchPart part) {
		return UIThreadRunnable.syncExec(() -> part.getSite().getSelectionProvider().getSelection());
	}
}
