package mesfavoris.java.editor;

import static mesfavoris.java.JavaBookmarkProperties.*;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class JavaEditorUtils {

	public static boolean isType(String elementKind) {
		return KIND_ANNOTATION.equals(elementKind)
				|| KIND_CLASS.equals(elementKind)				
				|| KIND_INTERFACE.equals(elementKind)
				|| KIND_ENUM.equals(elementKind)
				|| KIND_TYPE.equals(elementKind);
	}

	public static int getLineNumber(IMember member) throws JavaModelException,
			BadLocationException {
		ITypeRoot typeRoot = member.getTypeRoot();
		Document document = new Document(typeRoot.getBuffer().getContents());

		int offset = 0;
		if (SourceRange.isAvailable(member.getNameRange())) {
			offset = member.getNameRange().getOffset();
		} else if (SourceRange.isAvailable(member.getSourceRange())) {
			offset = member.getSourceRange().getOffset();
		}
		return document.getLineOfOffset(offset);
	}

	public static String getMethodSimpleSignature(IMethod method) {
		StringBuilder sb = new StringBuilder();
		try {
			sb.append(Signature.getSimpleName(Signature.toString(method
					.getReturnType())));

			sb.append(" ").append(method.getElementName());
			sb.append("(");
			int i = 0;
			for (String parameterType : method.getParameterTypes()) {
				if (i != 0) {
					sb.append(",");
				}
				sb.append(Signature.getSimpleName(Signature
						.toString(parameterType)));
				i++;
			}
			sb.append(")");
			return sb.toString();
		} catch (IllegalArgumentException e) {
			return null;
		} catch (JavaModelException e) {
			return null;
		}
	}

	public static void gotoLine(ITextEditor textEditor, int line)
			throws BadLocationException {
		IDocumentProvider provider = textEditor.getDocumentProvider();
		IDocument document = provider.getDocument(textEditor.getEditorInput());

		int start = document.getLineOffset(line);
		textEditor.selectAndReveal(start, 0);

		IWorkbenchPage page = textEditor.getSite().getPage();
		page.activate(textEditor);

	}	
	
}
