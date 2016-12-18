package mesfavoris.texteditor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class TextEditorUtils {

	public static void gotoLine(ITextEditor textEditor, int line)
			throws BadLocationException {
		IDocumentProvider provider = textEditor.getDocumentProvider();
		IDocument document = provider.getDocument(textEditor.getEditorInput());

		int start = document.getLineOffset(line);
		textEditor.selectAndReveal(start, 0);

		IWorkbenchPage page = textEditor.getSite().getPage();
		page.activate(textEditor);

	}

	public static String getLineContent(ITextEditor textEditor, int lineNumber) {
		try {
			IDocumentProvider provider = textEditor.getDocumentProvider();
			IDocument document = provider.getDocument(textEditor.getEditorInput());
			IRegion region = document.getLineInformation(lineNumber);
			return document.get(region.getOffset(), region.getLength());
		} catch (BadLocationException e) {
			return null;
		}
	}	
	
}
