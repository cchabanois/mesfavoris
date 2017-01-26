package mesfavoris.tests.commons.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public class SWTBotEditorHelper {

	public static SWTBotEclipseEditor textEditor(IProject project, String fileName) throws PartInitException {
		SWTWorkbenchBot bot = new SWTWorkbenchBot();
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

}
