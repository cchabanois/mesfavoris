package mesfavoris.java;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

import mesfavoris.bookmarktype.IBookmarkLocation;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.model.Bookmark;

public class GotoJavaBookmarkMarker implements IGotoBookmark {

	@Override
	public boolean gotoBookmark(IWorkbenchWindow window, Bookmark bookmark, IBookmarkLocation bookmarkLocation) {
		if (!(bookmarkLocation instanceof JavaMarkerBookmarkLocation)) {
			return false;
		}
		JavaMarkerBookmarkLocation javaMarkerBookmarkLocation = (JavaMarkerBookmarkLocation) bookmarkLocation;
		IJavaElement javaElement = JavaCore.create(javaMarkerBookmarkLocation.getHandle());
		if (javaElement == null) {
			return false;
		}
		IEditorInput editorInput = EditorUtility.getEditorInput(javaElement);
		if (editorInput == null) {
			return false;
		}
		String editorId = getEditorId(editorInput);
		if (editorId == null) {
			return false;
		}
		try {
			IEditorPart editorPart = IDE.openEditor(window.getActivePage(), editorInput, editorId);
			if (editorPart == null) {
				return false;
			}
			IDE.gotoMarker(editorPart, javaMarkerBookmarkLocation.getMarker());
			return true;
		} catch (PartInitException e) {
			return false;
		}
	}

	private String getEditorId(IEditorInput input) {
		try {
			IEditorDescriptor descriptor = IDE.getEditorDescriptor(input.getName());
			return descriptor.getId();
		} catch (PartInitException e) {
			return null;
		}
	}
}
