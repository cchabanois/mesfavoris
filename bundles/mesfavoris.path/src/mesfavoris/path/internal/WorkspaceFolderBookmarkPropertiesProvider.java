package mesfavoris.path.internal;

import static mesfavoris.path.PathBookmarkProperties.PROPERTY_NAME;
import static mesfavoris.path.PathBookmarkProperties.PROP_PROJECT_NAME;
import static mesfavoris.path.PathBookmarkProperties.PROP_WORKSPACE_PATH;
import static mesfavoris.path.PathBookmarkProperties.PROP_FOLDER_PATH;

import java.io.File;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

import mesfavoris.MesFavoris;
import mesfavoris.bookmarktype.AbstractBookmarkPropertiesProvider;
import mesfavoris.commons.core.AdapterUtils;
import mesfavoris.placeholders.IPathPlaceholderResolver;

public class WorkspaceFolderBookmarkPropertiesProvider extends AbstractBookmarkPropertiesProvider {
	private final IPathPlaceholderResolver pathPlaceholderResolver;

	public WorkspaceFolderBookmarkPropertiesProvider() {
		this(MesFavoris.getPathPlaceholderResolver());
	}

	public WorkspaceFolderBookmarkPropertiesProvider(IPathPlaceholderResolver pathPlaceholders) {
		this.pathPlaceholderResolver = pathPlaceholders;
	}
	
	@Override
	public void addBookmarkProperties(Map<String, String> bookmarkProperties, IWorkbenchPart part,
			ISelection selection, IProgressMonitor monitor) {
		Object selected = getFirstElement(selection);
		IResource resource = AdapterUtils.getAdapter(selected, IResource.class);
		if (!(resource instanceof IFolder)) {
			return;
		}
		IFolder folder = (IFolder)resource;
		
		putIfAbsent(bookmarkProperties, PROP_WORKSPACE_PATH, folder.getFullPath().toPortableString());
		putIfAbsent(bookmarkProperties, PROP_PROJECT_NAME, folder.getProject().getName());
		File localFile = folder.getLocation().toFile();
		IPath folderPath = Path.fromOSString(localFile.toString());
		addFolderPath(bookmarkProperties, folderPath);
		putIfAbsent(bookmarkProperties, PROPERTY_NAME, () ->  folderPath.lastSegment());
	}

	private void addFolderPath(Map<String, String> properties, IPath filePath) {
		putIfAbsent(properties, PROP_FOLDER_PATH, () -> pathPlaceholderResolver.collapse(filePath));
	}	
	
}
