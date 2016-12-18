package mesfavoris.bookmarktype;

import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;

import mesfavoris.commons.core.AdapterUtils;

public abstract class AbstractBookmarkPropertiesProvider implements IBookmarkPropertiesProvider {

	protected void putIfAbsent(Map<String, String> bookmarkProperties, String name, String value) {
		putIfAbsent(bookmarkProperties, name, () -> value);
	}

	protected void putIfAbsent(Map<String, String> bookmarkProperties, String name, Supplier<String> valueProvider) {
		if (bookmarkProperties.containsKey(name)) {
			return;
		}
		String value = valueProvider.get();
		if (value != null) {
			bookmarkProperties.put(name, value);
		}
	}

	protected Object getFirstElement(ISelection selection) {
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
	protected ITextEditor getTextEditor(IWorkbenchPart part) {
		ITextEditor[] textEditor = new ITextEditor[1];
		part.getSite().getWorkbenchWindow().getShell().getDisplay().syncExec(() -> {
			textEditor[0] = AdapterUtils.getAdapter(part, ITextEditor.class);
		});
		return textEditor[0];
	}

	protected ISelection getSelection(ITextEditor textEditor) {
		ISelection[] selection = new ISelection[1];
		textEditor.getSite().getWorkbenchWindow().getShell().getDisplay().syncExec(() -> {
			selection[0] = textEditor.getSelectionProvider().getSelection();
		});
		return selection[0];
	}

}
