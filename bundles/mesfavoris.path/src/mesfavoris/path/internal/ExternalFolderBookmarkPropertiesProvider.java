package mesfavoris.path.internal;

import static mesfavoris.path.PathBookmarkProperties.PROPERTY_NAME;
import static mesfavoris.path.PathBookmarkProperties.PROP_FOLDER_PATH;

import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

import mesfavoris.MesFavoris;
import mesfavoris.bookmarktype.AbstractBookmarkPropertiesProvider;
import mesfavoris.commons.core.AdapterUtils;
import mesfavoris.placeholders.IPathPlaceholderResolver;

public class ExternalFolderBookmarkPropertiesProvider extends AbstractBookmarkPropertiesProvider {
	private final IPathPlaceholderResolver pathPlaceholderResolver;

	public ExternalFolderBookmarkPropertiesProvider() {
		this(MesFavoris.getPathPlaceholderResolver());
	}

	public ExternalFolderBookmarkPropertiesProvider(IPathPlaceholderResolver pathPlaceholders) {
		this.pathPlaceholderResolver = pathPlaceholders;
	}

	public void addBookmarkProperties(Map<String, String> bookmarkProperties, IWorkbenchPart part, ISelection selection,
			IProgressMonitor monitor) {
		Object selected = getFirstElement(selection);
		IPath path = AdapterUtils.getAdapter(selected, IPath.class);
		if (path == null || !path.isAbsolute() || !path.toFile().isDirectory()) {
			return;
		}
		putIfAbsent(bookmarkProperties, PROPERTY_NAME, () -> path.lastSegment());
		putIfAbsent(bookmarkProperties, PROP_FOLDER_PATH, () -> pathPlaceholderResolver.collapse(path));
	}

}
