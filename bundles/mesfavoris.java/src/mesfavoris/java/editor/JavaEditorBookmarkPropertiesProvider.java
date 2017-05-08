package mesfavoris.java.editor;

import static mesfavoris.bookmarktype.BookmarkPropertiesProviderUtil.getSelection;
import static mesfavoris.bookmarktype.BookmarkPropertiesProviderUtil.getTextEditor;
import static mesfavoris.java.JavaBookmarkProperties.PROP_LINE_NUMBER_INSIDE_ELEMENT;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_LINE_CONTENT;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
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
		int lineNumber = textSelection.getStartLine();
		int offset = getOffset(editor, textSelection);
		IJavaElement editorJavaElement = JavaUI.getEditorInputJavaElement(editor.getEditorInput());
		if (editorJavaElement == null) {
			return;
		}
		IJavaElement containingJavaElement = getContainingJavaElement(editorJavaElement, offset);

		if (!(containingJavaElement instanceof IMember)) {
			return;
		}
		IMember member = (IMember) containingJavaElement;
		super.addMemberBookmarkProperties(bookmarkProperties, member);
		addLineNumberInsideMemberProperty(bookmarkProperties, member, lineNumber);
		addJavadocComment(bookmarkProperties, member, lineNumber);
		addLineContent(bookmarkProperties, editor, lineNumber);
	}

	private int getOffset(ITextEditor textEditor, ITextSelection textSelection) {
		try {
			int offset = TextEditorUtils.getOffsetOfFirstNonWhitespaceCharAtLine(textEditor, textSelection.getStartLine());
			return offset > textSelection.getOffset() ? offset : textSelection.getOffset();
		} catch (BadLocationException e) {
			return textSelection.getOffset();
		}
		
	}
	
	private void addLineNumberInsideMemberProperty(Map<String, String> bookmarkProperties, IMember member,
			int lineNumber) {
		try {
			int methodLineNumber = JavaEditorUtils.getLineNumber(member);
			int lineNumberInsideMethod = lineNumber - methodLineNumber;
			putIfAbsent(bookmarkProperties, PROP_LINE_NUMBER_INSIDE_ELEMENT, Integer.toString(lineNumberInsideMethod));
		} catch (JavaModelException e) {
			return;
		}
	}

	private void addJavadocComment(Map<String, String> bookmarkProperties, IMember member,
			int lineNumber) {
		try {
			if (JavaEditorUtils.getLineNumber(member) != lineNumber) {
				return;
			}
			super.addJavadocComment(bookmarkProperties, member);
		} catch (JavaModelException e) {
			return;
		}
	}

	private IJavaElement getContainingJavaElement(IJavaElement editorJavaElement, int offset) {
		if (!(editorJavaElement instanceof ITypeRoot)) {
			return null;
		}
		ITypeRoot compilationUnit = (ITypeRoot) editorJavaElement;
		try {
			IJavaElement selectedJavaElement = compilationUnit.getElementAt(offset);
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
