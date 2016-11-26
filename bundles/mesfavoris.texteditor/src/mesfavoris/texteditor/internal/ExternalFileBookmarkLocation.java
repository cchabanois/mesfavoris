package mesfavoris.texteditor.internal;

import org.eclipse.core.runtime.IPath;

import mesfavoris.bookmarktype.IBookmarkLocation;

public class ExternalFileBookmarkLocation implements IBookmarkLocation {
	private final IPath fileSystemPath;
	private final Integer lineNumber;
	private final Integer lineOffset;
	
	public ExternalFileBookmarkLocation(IPath fileSystemPath, Integer lineNumber, Integer lineOffset) {
		this.fileSystemPath = fileSystemPath;
		this.lineNumber = lineNumber;
		this.lineOffset = lineOffset;
	}

	public IPath getFileSystemPath() {
		return fileSystemPath;
	}

	public Integer getLineNumber() {
		return lineNumber;
	}
	
	public Integer getLineOffset() {
		return lineOffset;
	}
}
