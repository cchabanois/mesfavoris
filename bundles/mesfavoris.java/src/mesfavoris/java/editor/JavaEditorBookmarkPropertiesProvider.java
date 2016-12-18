package mesfavoris.java.editor;

import static mesfavoris.java.JavaBookmarkProperties.PROP_LINE_NUMBER_INSIDE_ELEMENT;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_LINE_CONTENT;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;

import mesfavoris.java.element.JavaTypeMemberBookmarkPropertiesProvider;
import mesfavoris.texteditor.TextEditorUtils;

public class JavaEditorBookmarkPropertiesProvider extends JavaTypeMemberBookmarkPropertiesProvider {

	@Override
	public void addBookmarkProperties(Map<String, String> bookmarkProperties, IWorkbenchPart part, ISelection selection,
			IProgressMonitor monitor) {
		ITextEditor editor = getTextEditor(part);
		if (editor == null) {
			return;
		}
		if (editor != part) {
			selection = getSelection(editor);
		}
		if (!(selection instanceof ITextSelection)) {
			return;
		}
		ITextSelection textSelection = (ITextSelection) selection;
		IJavaElement editorJavaElement = JavaUI.getEditorInputJavaElement(editor.getEditorInput());
		if (editorJavaElement == null) {
			return;
		}
		IJavaElement containingJavaElement = getContainingJavaElement(editorJavaElement, textSelection);

		if (!(containingJavaElement instanceof IMember)) {
			return;
		}
		IMember member = (IMember) containingJavaElement;
		super.addMemberBookmarkProperties(bookmarkProperties, member);
		addLineNumberInsideMemberProperty(bookmarkProperties, member, textSelection);
		addJavadocComment(bookmarkProperties, member, textSelection);
		addLineContent(bookmarkProperties, editor, textSelection.getStartLine());
	}

	private void addLineNumberInsideMemberProperty(Map<String, String> bookmarkProperties, IMember member,
			ITextSelection textSelection) {
		try {
			int methodLineNumber = JavaEditorUtils.getLineNumber(member);
			int lineNumber = textSelection.getStartLine();
			int lineNumberInsideMethod = lineNumber - methodLineNumber;
			putIfAbsent(bookmarkProperties, PROP_LINE_NUMBER_INSIDE_ELEMENT, Integer.toString(lineNumberInsideMethod));
		} catch (JavaModelException e) {
			return;
		}
	}

	private void addJavadocComment(Map<String, String> bookmarkProperties, IMember member,
			ITextSelection textSelection) {
		try {
			if (JavaEditorUtils.getLineNumber(member) != textSelection.getStartLine()) {
				return;
			}
			super.addJavadocComment(bookmarkProperties, member);
		} catch (JavaModelException e) {
			return;
		}
	}

	private IJavaElement getContainingJavaElement(IJavaElement editorJavaElement, ITextSelection textSelection) {
		if (!(editorJavaElement instanceof ITypeRoot)) {
			return null;
		}
		ITypeRoot compilationUnit = (ITypeRoot) editorJavaElement;
		try {
			IJavaElement selectedJavaElement = compilationUnit.getElementAt(textSelection.getOffset());
			return selectedJavaElement;
		} catch (JavaModelException e) {
			return null;
		}
	}

	private void addLineContent(Map<String, String> properties, ITextEditor textEditor, int lineNumber) {
		putIfAbsent(properties, PROP_LINE_CONTENT, () -> {
			String content = TextEditorUtils.getLineContent(textEditor, lineNumber);
			return content == null ? null : content.trim();
		});
	}	
	
}
