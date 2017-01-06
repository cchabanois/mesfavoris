package mesfavoris.path.internal;

import org.eclipse.core.resources.IFolder;

import mesfavoris.bookmarktype.IBookmarkLocation;

public class WorkspaceFolderBookmarkLocation implements IBookmarkLocation {
	private final IFolder folder;

	public WorkspaceFolderBookmarkLocation(IFolder folder) {
		this.folder = folder;
	}

	public IFolder getWorkspaceFolder() {
		return folder;
	}

}
