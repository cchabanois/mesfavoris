package mesfavoris.javascript;

import static mesfavoris.javascript.JavascriptBookmarkProperties.KIND_ANNOTATION;
import static mesfavoris.javascript.JavascriptBookmarkProperties.KIND_CLASS;
import static mesfavoris.javascript.JavascriptBookmarkProperties.KIND_ENUM;
import static mesfavoris.javascript.JavascriptBookmarkProperties.KIND_INTERFACE;
import static mesfavoris.javascript.JavascriptBookmarkProperties.KIND_TYPE;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wst.jsdt.core.IFunction;
import org.eclipse.wst.jsdt.core.IMember;
import org.eclipse.wst.jsdt.core.ITypeRoot;
import org.eclipse.wst.jsdt.core.JavaScriptModelException;
import org.eclipse.wst.jsdt.core.Signature;
import org.eclipse.wst.jsdt.internal.corext.SourceRange;

public class JavascriptEditorUtils {

	public static boolean isType(String elementKind) {
		return KIND_ANNOTATION.equals(elementKind)
				|| KIND_CLASS.equals(elementKind)				
				|| KIND_INTERFACE.equals(elementKind)
				|| KIND_ENUM.equals(elementKind)
				|| KIND_TYPE.equals(elementKind);
	}

	public static int getLineNumber(IMember member) throws JavaScriptModelException,
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

	public static String getMethodSimpleSignature(IFunction method) {
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
		} catch (JavaScriptModelException e) {
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
