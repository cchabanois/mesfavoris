package mesfavoris.texteditor.internal;

import org.eclipse.core.runtime.IPath;

import mesfavoris.bookmarktype.IBookmarkLocation;

public class ExternalFolderBookmarkLocation implements IBookmarkLocation {
	private final IPath fileSystemPath;
	
	public ExternalFolderBookmarkLocation(IPath fileSystemPath) {
		this.fileSystemPath = fileSystemPath;
	}

	public IPath getFileSystemPath() {
		return fileSystemPath;
	}

}
