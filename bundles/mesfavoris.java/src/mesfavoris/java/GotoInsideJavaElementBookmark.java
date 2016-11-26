package mesfavoris.java;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.ITextEditor;

import mesfavoris.bookmarktype.IBookmarkLocation;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.commons.core.AdapterUtils;
import mesfavoris.java.editor.JavaEditorUtils;
import mesfavoris.model.Bookmark;

public class GotoInsideJavaElementBookmark implements IGotoBookmark {

	@Override
	public boolean gotoBookmark(IWorkbenchWindow window, Bookmark bookmark, IBookmarkLocation bookmarkLocation) {
		if (!(bookmarkLocation instanceof JavaTypeMemberBookmarkLocation)) {
			return false;
		}
		JavaTypeMemberBookmarkLocation location = (JavaTypeMemberBookmarkLocation) bookmarkLocation;
		IEditorPart editorPart = openInEditor(location.getMember());;
		ITextEditor textEditor = AdapterUtils.getAdapter(editorPart, ITextEditor.class);
		if (textEditor == null) {
			return false;
		}
		if (location.getLineNumber() != null) {
			try {
				JavaEditorUtils.gotoLine(textEditor, location.getLineNumber());
			} catch (BadLocationException e) {
				return false;
			}
		}
		return true;
	}

	private IEditorPart openInEditor(IJavaElement javaElement) {
		try {
			return JavaUI.openInEditor(javaElement, true, true);
		} catch (PartInitException e) {
			return null;
		} catch (JavaModelException e) {
			return null;
		}
	}

}
