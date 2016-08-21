package mesfavoris.texteditor.internal;

import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_WORKSPACE_PATH;

import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import mesfavoris.model.Bookmark;
import mesfavoris.texteditor.TextEditorBookmarkProperties;
import mesfavoris.texteditor.resource.FuzzyResourceFinder;

public class WorkspaceFileBookmarkLocationProvider extends AbstractFileBookmarkLocationProvider {

	@Override
	public WorkspaceFileBookmarkLocation getBookmarkLocation(Bookmark bookmark, IProgressMonitor monitor) {
		Optional<IFile> workspaceFile = getWorkspaceFile(bookmark);
		if (!workspaceFile.isPresent()) {
			return null;
		}
		IPath filePath = workspaceFile.get().getLocation();
		String lineContent = bookmark.getPropertyValue(TextEditorBookmarkProperties.PROP_LINE_CONTENT);
		Integer lineNumber = getExpectedLineNumber(bookmark);
		if (lineContent != null && filePath != null) {
			lineNumber = getLineNumber(filePath, lineNumber, lineContent, monitor);
		}
		return new WorkspaceFileBookmarkLocation(workspaceFile.get(), lineNumber);
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
