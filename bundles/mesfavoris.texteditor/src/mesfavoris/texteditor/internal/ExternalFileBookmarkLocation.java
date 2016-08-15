package mesfavoris.texteditor.internal;

import org.eclipse.core.runtime.IPath;

import mesfavoris.bookmarktype.IBookmarkLocation;

public class ExternalFileBookmarkLocation implements IBookmarkLocation {
	private final IPath fileSystemPath;
	private final Integer lineNumber;

	public ExternalFileBookmarkLocation(IPath fileSystemPath, Integer lineNumber) {
		this.fileSystemPath = fileSystemPath;
		this.lineNumber = lineNumber;
	}

	public IPath getFileSystemPath() {
		return fileSystemPath;
	}

	public Integer getLineNumber() {
		return lineNumber;
	}
	
	
}
