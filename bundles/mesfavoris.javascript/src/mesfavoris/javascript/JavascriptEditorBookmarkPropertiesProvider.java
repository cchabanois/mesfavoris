package mesfavoris.javascript;

import static mesfavoris.javascript.JavascriptBookmarkProperties.KIND_CLASS;
import static mesfavoris.javascript.JavascriptBookmarkProperties.KIND_FIELD;
import static mesfavoris.javascript.JavascriptBookmarkProperties.KIND_INITIALIZER;
import static mesfavoris.javascript.JavascriptBookmarkProperties.KIND_METHOD;
import static mesfavoris.javascript.JavascriptBookmarkProperties.KIND_TYPE;
import static mesfavoris.javascript.JavascriptBookmarkProperties.PROP_JAVASCRIPT_DECLARING_TYPE;
import static mesfavoris.javascript.JavascriptBookmarkProperties.PROP_JAVASCRIPT_ELEMENT_KIND;
import static mesfavoris.javascript.JavascriptBookmarkProperties.PROP_JAVASCRIPT_ELEMENT_NAME;
import static mesfavoris.javascript.JavascriptBookmarkProperties.PROP_JAVASCRIPT_METHOD_SIGNATURE;
import static mesfavoris.javascript.JavascriptBookmarkProperties.PROP_JAVASCRIPT_TYPE;
import static mesfavoris.javascript.JavascriptBookmarkProperties.PROP_LINE_NUMBER_INSIDE_ELEMENT;

import java.util.Map;

import org.chabanois.mesfavoris.bookmarktype.AbstractBookmarkPropertiesProvider;
import org.chabanois.mesfavoris.model.Bookmark;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wst.jsdt.core.IField;
import org.eclipse.wst.jsdt.core.IFunction;
import org.eclipse.wst.jsdt.core.IJavaScriptElement;
import org.eclipse.wst.jsdt.core.IMember;
import org.eclipse.wst.jsdt.core.IType;
import org.eclipse.wst.jsdt.core.ITypeRoot;
import org.eclipse.wst.jsdt.core.JavaScriptModelException;
import org.eclipse.wst.jsdt.ui.JavaScriptUI;

public class JavascriptEditorBookmarkPropertiesProvider extends
		AbstractBookmarkPropertiesProvider {

	@Override
	public void addBookmarkProperties(Map<String, String> bookmarkProperties,
			Object selected) {
		if (!(selected instanceof ITextEditor)) {
			return;
		}
		ITextEditor editor = (ITextEditor) selected;
		IJavaScriptElement editorJavaElement = JavaScriptUI
				.getEditorInputJavaElement(editor.getEditorInput());
		if (editorJavaElement == null) {
			return;
		}
		ITextSelection textSelection = (ITextSelection) editor
				.getSelectionProvider().getSelection();
		IJavaScriptElement containingJavaElement = getContainingJavaElement(
				editorJavaElement, textSelection);

		if (!(containingJavaElement instanceof IMember)) {
			return;
		}
		IMember member = (IMember) containingJavaElement;
		addMemberBookmarkProperties(bookmarkProperties, member);
		addLineNumberInsideMemberProperty(bookmarkProperties, member,
				textSelection);
	}

	private IJavaScriptElement getContainingJavaElement(
			IJavaScriptElement editorJavaElement, ITextSelection textSelection) {
		if (!(editorJavaElement instanceof ITypeRoot)) {
			return null;
		}
		ITypeRoot compilationUnit = (ITypeRoot) editorJavaElement;

		try {
			IJavaScriptElement selectedJavaElement = selectedJavaElement = compilationUnit
					.getElementAt(textSelection.getOffset());
			return selectedJavaElement;
		} catch (JavaScriptModelException e) {
			return null;
		}

	}

	private void addLineNumberInsideMemberProperty(
			Map<String, String> bookmarkProperties, IMember member,
			ITextSelection textSelection) {
		try {
			int methodLineNumber = JavascriptEditorUtils.getLineNumber(member);
			int lineNumber = textSelection.getStartLine();
			int lineNumberInsideMethod = lineNumber - methodLineNumber;
			putIfAbsent(bookmarkProperties, PROP_LINE_NUMBER_INSIDE_ELEMENT,
					Integer.toString(lineNumberInsideMethod));
		} catch (JavaScriptModelException e) {
			return;
		} catch (BadLocationException e) {
			return;
		}

	}	
	
	private void addMemberBookmarkProperties(
			Map<String, String> bookmarkProperties, IMember member) {
		putIfAbsent(bookmarkProperties, PROP_JAVASCRIPT_ELEMENT_NAME,
				member.getElementName());
		if (member.getDeclaringType() != null) {
			putIfAbsent(bookmarkProperties, PROP_JAVASCRIPT_DECLARING_TYPE, member
					.getDeclaringType().getFullyQualifiedName());
		}
		putIfAbsent(bookmarkProperties, PROP_JAVASCRIPT_ELEMENT_KIND, getKind(member));
		if (member instanceof IFunction) {
			putIfAbsent(bookmarkProperties, PROP_JAVASCRIPT_METHOD_SIGNATURE,
					JavascriptEditorUtils.getMethodSimpleSignature((IFunction) member));
			putIfAbsent(bookmarkProperties, Bookmark.PROPERTY_NAME,
					bookmarkProperties.get(PROP_JAVASCRIPT_DECLARING_TYPE) + '.'
							+ member.getElementName() + "()");
		}
		if (member instanceof IType) {
			IType type = (IType) member;
			putIfAbsent(bookmarkProperties, PROP_JAVASCRIPT_TYPE,
					type.getFullyQualifiedName());
			putIfAbsent(bookmarkProperties, Bookmark.PROPERTY_NAME,
					type.getFullyQualifiedName());
		}
		if (member instanceof IField) {
			putIfAbsent(bookmarkProperties, Bookmark.PROPERTY_NAME,
					bookmarkProperties.get(PROP_JAVASCRIPT_DECLARING_TYPE) + '.'
							+ member.getElementName());
		}
		putIfAbsent(bookmarkProperties, Bookmark.PROPERTY_NAME,
				member.getElementName());
	}	
	
	private String getKind(IMember member) {
		switch (member.getElementType()) {
		case IJavaScriptElement.METHOD:
			return KIND_METHOD;
		case IJavaScriptElement.INITIALIZER:
			return KIND_INITIALIZER;
		case IJavaScriptElement.FIELD:
			return KIND_FIELD;
		case IJavaScriptElement.TYPE:
			IType type = (IType) member;
			try {
				if (type.isClass()) {
					return KIND_CLASS;
				}
			} catch (JavaScriptModelException e) {
				return KIND_TYPE;
			}
			return KIND_TYPE;
		default:
			return null;
		}
	}		
	
}
