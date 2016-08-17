package mesfavoris.texteditor.internal;

import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_WORKSPACE_PATH;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import mesfavoris.model.Bookmark;
import mesfavoris.texteditor.TextEditorBookmarkProperties;

public class WorkspaceFileBookmarkLocationProvider extends AbstractFileBookmarkLocationProvider {

	@Override
	public WorkspaceFileBookmarkLocation getBookmarkLocation(Bookmark bookmark, IProgressMonitor monitor) {
		IFile workspaceFile = getWorkspaceFile(bookmark);
		if (workspaceFile == null || !workspaceFile.exists()) {
			return null;
		}
		IPath filePath = workspaceFile.getLocation();
		String lineContent = bookmark.getPropertyValue(TextEditorBookmarkProperties.PROP_LINE_CONTENT);
		Integer lineNumber = getExpectedLineNumber(bookmark);
		if (lineContent != null && filePath != null) {
			lineNumber = getLineNumber(filePath, lineNumber, lineContent, monitor);
		}
		return new WorkspaceFileBookmarkLocation(workspaceFile, lineNumber);
	}

	private IFile getWorkspaceFile(Bookmark bookmark) {
		String workspacePath = bookmark.getPropertyValue(PROP_WORKSPACE_PATH);
		if (workspacePath == null) {
			return null;
		}
		Path path = new Path(workspacePath);
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		return file;
	}

}
