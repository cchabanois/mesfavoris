package mesfavoris.java.editor;

import static mesfavoris.java.JavaBookmarkProperties.PROP_LINE_NUMBER_INSIDE_ELEMENT;

import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.texteditor.ITextEditor;

import mesfavoris.java.element.JavaTypeMemberBookmarkPropertiesProvider;

public class JavaEditorBookmarkPropertiesProvider extends
		JavaTypeMemberBookmarkPropertiesProvider {

	@Override
	public void addBookmarkProperties(Map<String, String> bookmarkProperties,
			Object selected) {
		if (!(selected instanceof ITextEditor)) {
			return;
		}
		ITextEditor editor = (ITextEditor) selected;
		IJavaElement editorJavaElement = JavaUI
				.getEditorInputJavaElement(editor.getEditorInput());
		if (editorJavaElement == null) {
			return;
		}
		ITextSelection textSelection = (ITextSelection) editor
				.getSelectionProvider().getSelection();
		IJavaElement containingJavaElement = getContainingJavaElement(
				editorJavaElement, textSelection);

		if (!(containingJavaElement instanceof IMember)) {
			return;
		}
		IMember member = (IMember) containingJavaElement;
		super.addBookmarkProperties(bookmarkProperties, member);
		addLineNumberInsideMemberProperty(bookmarkProperties, member,
				textSelection);
	}

	private void addLineNumberInsideMemberProperty(
			Map<String, String> bookmarkProperties, IMember member,
			ITextSelection textSelection) {
		try {
			int methodLineNumber = JavaEditorUtils.getLineNumber(member);
			int lineNumber = textSelection.getStartLine();
			int lineNumberInsideMethod = lineNumber - methodLineNumber;
			putIfAbsent(bookmarkProperties, PROP_LINE_NUMBER_INSIDE_ELEMENT,
					Integer.toString(lineNumberInsideMethod));
		} catch (JavaModelException e) {
			return;
		} catch (BadLocationException e) {
			return;
		}

	}

	private IJavaElement getContainingJavaElement(
			IJavaElement editorJavaElement, ITextSelection textSelection) {
		if (!(editorJavaElement instanceof ITypeRoot)) {
			return null;
		}
		ITypeRoot compilationUnit = (ITypeRoot) editorJavaElement;
		try {
			IJavaElement selectedJavaElement = compilationUnit
					.getElementAt(textSelection.getOffset());
			return selectedJavaElement;
		} catch (JavaModelException e) {
			return null;
		}
	}

}
