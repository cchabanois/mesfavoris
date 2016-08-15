package mesfavoris.texteditor.internal;

import org.eclipse.core.resources.IFile;

import mesfavoris.bookmarktype.IBookmarkLocation;

public class WorkspaceFileBookmarkLocation implements IBookmarkLocation {
	private final IFile file;
	private final Integer lineNumber;
	
	public WorkspaceFileBookmarkLocation(IFile file, Integer lineNumber) {
		this.file = file;
		this.lineNumber = lineNumber;
	}
	
	public IFile getWorkspaceFile() {
		return file;
	}
	
	public Integer getLineNumber() {
		return lineNumber;
	}
}
