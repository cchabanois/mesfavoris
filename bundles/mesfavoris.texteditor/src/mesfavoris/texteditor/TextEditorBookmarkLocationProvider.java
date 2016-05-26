package mesfavoris.texteditor;

import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_FILE_PATH;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_LINE_NUMBER;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_WORKSPACE_PATH;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import mesfavoris.model.Bookmark;
import mesfavoris.texteditor.placeholders.PathPlaceholderResolver;

public class TextEditorBookmarkLocationProvider {
	private final PathPlaceholderResolver pathPlaceholderResolver;

	public TextEditorBookmarkLocationProvider(PathPlaceholderResolver pathPlaceholderResolver) {
		this.pathPlaceholderResolver = pathPlaceholderResolver;
	}

	public TextEditorBookmarkLocation findLocation(Bookmark bookmark) {
		IFile workspaceFile = getWorkspaceFile(bookmark);
		String nonExpandedFilePath = bookmark.getPropertyValue(PROP_FILE_PATH);
		IPath filePath = nonExpandedFilePath != null ? pathPlaceholderResolver.expand(nonExpandedFilePath) : null;
		if (workspaceFile == null && filePath == null) {
			return null;
		}
		String lineNumberAsString = bookmark.getPropertyValue(PROP_LINE_NUMBER);
		Integer lineNumber = lineNumberAsString == null ? null : Integer.parseInt(lineNumberAsString);
		return new TextEditorBookmarkLocation(workspaceFile, filePath, lineNumber);
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

	public static class TextEditorBookmarkLocation {
		private final IFile workspaceFile;
		private final IPath fileSystemPath;
		private final Integer lineNumber;

		public TextEditorBookmarkLocation(IFile workspaceFile, IPath fileSystemPath, Integer lineNumber) {
			this.workspaceFile = workspaceFile;
			this.fileSystemPath = fileSystemPath;
			this.lineNumber = lineNumber;
		}

		public IFile getWorkspaceFile() {
			return workspaceFile;
		}

		public IPath getFileSystemPath() {
			return fileSystemPath;
		}

		public Integer getLineNumber() {
			return lineNumber;
		}

	}

}
