package mesfavoris.texteditor;

import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROPERTY_NAME;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_FILE_PATH;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_PROJECT_NAME;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_WORKSPACE_PATH;

import java.io.File;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

import mesfavoris.bookmarktype.AbstractBookmarkPropertiesProvider;
import mesfavoris.commons.core.AdapterUtils;
import mesfavoris.texteditor.placeholders.PathPlaceholderResolver;

public class FileBookmarkPropertiesProvider extends AbstractBookmarkPropertiesProvider {
	private final PathPlaceholderResolver pathPlaceholderResolver;

	public FileBookmarkPropertiesProvider() {
		this(new PathPlaceholderResolver(Activator.getPathPlaceholdersStore()));
	}

	public FileBookmarkPropertiesProvider(PathPlaceholderResolver pathPlaceholders) {
		this.pathPlaceholderResolver = pathPlaceholders;
	}
	
	@Override
	public void addBookmarkProperties(Map<String, String> bookmarkProperties, IWorkbenchPart part,
			ISelection selection) {
		Object selected = getFirstElement(selection);
		IResource resource = AdapterUtils.getAdapter(selected, IResource.class);
		if (!(resource instanceof IFile)) {
			return;
		}
		IFile file = (IFile)resource;
		
		putIfAbsent(bookmarkProperties, PROP_WORKSPACE_PATH, file.getFullPath().toPortableString());
		putIfAbsent(bookmarkProperties, PROP_PROJECT_NAME, file.getProject().getName());
		File localFile = file.getLocation().toFile();
		IPath filePath = Path.fromOSString(localFile.toString());
		addFilePath(bookmarkProperties, filePath);
		putIfAbsent(bookmarkProperties, PROPERTY_NAME, () ->  filePath.lastSegment());
	}

	private void addFilePath(Map<String, String> properties, IPath filePath) {
		putIfAbsent(properties, PROP_FILE_PATH, () -> pathPlaceholderResolver.collapse(filePath));
	}	
	
}
