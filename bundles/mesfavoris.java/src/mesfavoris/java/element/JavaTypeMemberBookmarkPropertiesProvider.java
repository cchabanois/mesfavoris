package mesfavoris.java.element;

import static mesfavoris.java.JavaBookmarkProperties.KIND_ANNOTATION;
import static mesfavoris.java.JavaBookmarkProperties.KIND_CLASS;
import static mesfavoris.java.JavaBookmarkProperties.KIND_ENUM;
import static mesfavoris.java.JavaBookmarkProperties.KIND_FIELD;
import static mesfavoris.java.JavaBookmarkProperties.KIND_INITIALIZER;
import static mesfavoris.java.JavaBookmarkProperties.KIND_INTERFACE;
import static mesfavoris.java.JavaBookmarkProperties.KIND_METHOD;
import static mesfavoris.java.JavaBookmarkProperties.KIND_TYPE;
import static mesfavoris.java.JavaBookmarkProperties.PROP_JAVA_DECLARING_TYPE;
import static mesfavoris.java.JavaBookmarkProperties.PROP_JAVA_ELEMENT_KIND;
import static mesfavoris.java.JavaBookmarkProperties.PROP_JAVA_ELEMENT_NAME;
import static mesfavoris.java.JavaBookmarkProperties.PROP_JAVA_METHOD_SIGNATURE;
import static mesfavoris.java.JavaBookmarkProperties.PROP_JAVA_TYPE;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

import mesfavoris.bookmarktype.AbstractBookmarkPropertiesProvider;
import mesfavoris.java.editor.JavaEditorUtils;
import mesfavoris.model.Bookmark;

public class JavaTypeMemberBookmarkPropertiesProvider extends AbstractBookmarkPropertiesProvider {

	@Override
	public void addBookmarkProperties(Map<String, String> bookmarkProperties,IWorkbenchPart part,
			ISelection selection, IProgressMonitor monitor) {
		Object selected = getFirstElement(selection);
		if (selected instanceof ITypeRoot) {
			selected = ((ITypeRoot)selected).findPrimaryType();
		}
		if (!(selected instanceof IMember)) {
			return;
		}
		IMember member = (IMember)selected;
		addMemberBookmarkProperties(bookmarkProperties, member);
	}

	private void addMemberBookmarkProperties(
			Map<String, String> bookmarkProperties, IMember member) {
		putIfAbsent(bookmarkProperties, PROP_JAVA_ELEMENT_NAME,
				member.getElementName());
		if (member.getDeclaringType() != null) {
			putIfAbsent(bookmarkProperties, PROP_JAVA_DECLARING_TYPE, member
					.getDeclaringType().getFullyQualifiedName());
		}
		putIfAbsent(bookmarkProperties, PROP_JAVA_ELEMENT_KIND, getKind(member));
		if (member instanceof IMethod) {
			putIfAbsent(bookmarkProperties, PROP_JAVA_METHOD_SIGNATURE,
					JavaEditorUtils.getMethodSimpleSignature((IMethod) member));
			putIfAbsent(bookmarkProperties, Bookmark.PROPERTY_NAME,
					bookmarkProperties.get(PROP_JAVA_DECLARING_TYPE) + '.'
							+ member.getElementName() + "()");
		}
		if (member instanceof IType) {
			IType type = (IType) member;
			putIfAbsent(bookmarkProperties, PROP_JAVA_TYPE,
					type.getFullyQualifiedName());
			putIfAbsent(bookmarkProperties, Bookmark.PROPERTY_NAME,
					type.getFullyQualifiedName());
		}
		if (member instanceof IField) {
			putIfAbsent(bookmarkProperties, Bookmark.PROPERTY_NAME,
					bookmarkProperties.get(PROP_JAVA_DECLARING_TYPE) + '.'
							+ member.getElementName());
		}
		putIfAbsent(bookmarkProperties, Bookmark.PROPERTY_NAME,
				member.getElementName());
	}

	private String getKind(IMember member) {
		switch (member.getElementType()) {
		case IJavaElement.METHOD:
			return KIND_METHOD;
		case IJavaElement.INITIALIZER:
			return KIND_INITIALIZER;
		case IJavaElement.FIELD:
			return KIND_FIELD;
		case IJavaElement.TYPE:
			IType type = (IType) member;
			try {
				if (type.isAnnotation()) {
					return KIND_ANNOTATION;
				}
				if (type.isInterface()) {
					return KIND_INTERFACE;
				}
				if (type.isEnum()) {
					return KIND_ENUM;
				}
				if (type.isClass()) {
					return KIND_CLASS;
				}
			} catch (JavaModelException e) {
				return KIND_TYPE;
			}
			return KIND_TYPE;
		default:
			return null;
		}
	}	
	
}
