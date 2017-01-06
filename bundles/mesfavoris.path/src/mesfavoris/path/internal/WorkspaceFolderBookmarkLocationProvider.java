package mesfavoris.path.internal;

import static mesfavoris.path.PathBookmarkProperties.PROP_WORKSPACE_PATH;

import java.util.Optional;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import mesfavoris.bookmarktype.IBookmarkLocationProvider;
import mesfavoris.model.Bookmark;
import mesfavoris.path.resource.FuzzyResourceFinder;

public class WorkspaceFolderBookmarkLocationProvider implements IBookmarkLocationProvider {

	@Override
	public WorkspaceFolderBookmarkLocation getBookmarkLocation(Bookmark bookmark, IProgressMonitor monitor) {
		Optional<IFolder> workspaceFolder = getWorkspaceFolder(bookmark);
		if (!workspaceFolder.isPresent()) {
			return null;
		}
		return new WorkspaceFolderBookmarkLocation(workspaceFolder.get());
	}

	private Optional<IFolder> getWorkspaceFolder(Bookmark bookmark) {
		String workspacePath = bookmark.getPropertyValue(PROP_WORKSPACE_PATH);
		if (workspacePath == null) {
			return Optional.empty();
		}
		Path path = new Path(workspacePath);
		FuzzyResourceFinder fuzzyResourceFinder = new FuzzyResourceFinder();
		return fuzzyResourceFinder.find(path, IResource.FOLDER).map(IFolder.class::cast);
	}

}
