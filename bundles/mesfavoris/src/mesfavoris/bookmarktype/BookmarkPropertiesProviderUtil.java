package mesfavoris.bookmarktype;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Some common utilities for working with {@link IBookmarkPropertiesProvider}
 * 
 * @author cchabanois
 *
 */
public class BookmarkPropertiesProviderUtil {

	/**
	 * Get first element of given selection
	 * 
	 * @param selection
	 * @return first element of selection or null if none
	 */
	public static Object getFirstElement(ISelection selection) {
		if (!(selection instanceof IStructuredSelection)) {
			return null;
		}
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		return structuredSelection.getFirstElement();
	}

	/**
	 * Get current text editor for given {@link IWorkbenchPart}
	 * 
	 * @param part
	 * @return the current text editor or null if none
	 */
	public static ITextEditor getTextEditor(IWorkbenchPart part) {
		if (part == null) {
			return null;
		}
		ITextEditor[] textEditor = new ITextEditor[1];
		part.getSite().getWorkbenchWindow().getShell().getDisplay().syncExec(() -> {
			textEditor[0] = Adapters.adapt(part, ITextEditor.class);
		});
		return textEditor[0];
	}

	/**
	 * Get selection from text editor
	 * 
	 * @param textEditor
	 * @return
	 */
	public static ISelection getSelection(ITextEditor textEditor) {
		ISelection[] selection = new ISelection[1];
		textEditor.getSite().getWorkbenchWindow().getShell().getDisplay().syncExec(() -> {
			selection[0] = textEditor.getSelectionProvider().getSelection();
		});
		return selection[0];
	}

}
