package mesfavoris.tests.commons.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
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

	public static void closeEditor(IEditorPart editor) {
		IWorkbenchPartSite site;
		IWorkbenchPage page;
		if (editor != null && (site= editor.getSite()) != null && (page= site.getPage()) != null) {
			UIThreadRunnable.syncExec(()->page.closeEditor(editor, false));
		}
	}
	
	public static void closeAllEditors() {
		IWorkbenchWindow[] windows= PlatformUI.getWorkbench().getWorkbenchWindows();
		for (int i= 0; i < windows.length; i++) {
			IWorkbenchPage[] pages= windows[i].getPages();
			for (int j= 0; j < pages.length; j++) {
				IEditorReference[] editorReferences= pages[j].getEditorReferences();
				for (int k= 0; k < editorReferences.length; k++)
					closeEditor(editorReferences[k].getEditor(false));
			}
		}
	}	
	
}
