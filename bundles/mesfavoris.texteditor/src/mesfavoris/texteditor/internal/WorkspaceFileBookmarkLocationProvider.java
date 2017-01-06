package mesfavoris.texteditor.internal;

import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_WORKSPACE_PATH;

import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import mesfavoris.model.Bookmark;
import mesfavoris.path.resource.FuzzyResourceFinder;
import mesfavoris.texteditor.TextEditorBookmarkProperties;
import mesfavoris.texteditor.text.DocumentUtils;

public class WorkspaceFileBookmarkLocationProvider extends AbstractFileBookmarkLocationProvider {

	@Override
	public WorkspaceFileBookmarkLocation getBookmarkLocation(Bookmark bookmark, IProgressMonitor monitor) {
		Optional<IFile> workspaceFile = getWorkspaceFile(bookmark);
		if (!workspaceFile.isPresent()) {
			return null;
		}
		String lineContent = bookmark.getPropertyValue(TextEditorBookmarkProperties.PROP_LINE_CONTENT);
		Integer lineNumber = getExpectedLineNumber(bookmark);
		Integer lineOffset = null;
		Optional<IDocument> document = getDocument(workspaceFile.get());
		if (lineContent != null && document.isPresent()) {
			lineNumber = getLineNumber(document.get(), lineNumber, lineContent, monitor);
		}
		if (document.isPresent() && lineNumber != null) {
			lineOffset = getLineOffset(document.get(), lineNumber);
		}
		return new WorkspaceFileBookmarkLocation(workspaceFile.get(), lineNumber, lineOffset);
	}

	private Optional<IDocument> getDocument(IFile workspaceFile) {
		try {
			IPath filePath = workspaceFile.getLocation();
			return Optional.of(DocumentUtils.getDocument(filePath));
		} catch (CoreException e) {
			StatusHelper.logWarn("Could not get document", e);
			return Optional.empty();
		}
	}

	private Integer getLineOffset(IDocument document, int lineNumber) {
		try {
			return document.getLineOffset(lineNumber);
		} catch (BadLocationException e) {
			return null;
		}
	}

	private Optional<IFile> getWorkspaceFile(Bookmark bookmark) {
		String workspacePath = bookmark.getPropertyValue(PROP_WORKSPACE_PATH);
		if (workspacePath == null) {
			return Optional.empty();
		}
		Path path = new Path(workspacePath);
		FuzzyResourceFinder fuzzyResourceFinder = new FuzzyResourceFinder();
		return fuzzyResourceFinder.find(path, IResource.FILE).map(IFile.class::cast);
	}

}
