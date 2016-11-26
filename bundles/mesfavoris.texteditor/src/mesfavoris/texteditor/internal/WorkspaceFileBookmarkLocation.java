package mesfavoris.texteditor.internal;

import org.eclipse.core.resources.IFile;

import mesfavoris.bookmarktype.IBookmarkLocation;

public class WorkspaceFileBookmarkLocation implements IBookmarkLocation {
	private final IFile file;
	private final Integer lineNumber;
	private final Integer lineOffset;
	
	public WorkspaceFileBookmarkLocation(IFile file, Integer lineNumber, Integer lineOffset) {
		this.file = file;
		this.lineNumber = lineNumber;
		this.lineOffset = lineOffset;
	}
	
	public IFile getWorkspaceFile() {
		return file;
	}
	
	public Integer getLineNumber() {
		return lineNumber;
	}
	
	public Integer getLineOffset() {
		return lineOffset;
	}
}
