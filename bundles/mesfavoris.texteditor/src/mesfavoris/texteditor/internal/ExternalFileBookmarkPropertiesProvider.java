package mesfavoris.texteditor.internal;

import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROPERTY_NAME;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_FILE_PATH;

import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

import mesfavoris.bookmarktype.AbstractBookmarkPropertiesProvider;
import mesfavoris.commons.core.AdapterUtils;
import mesfavoris.texteditor.Activator;
import mesfavoris.texteditor.placeholders.PathPlaceholderResolver;

public class ExternalFileBookmarkPropertiesProvider extends AbstractBookmarkPropertiesProvider {
	private final PathPlaceholderResolver pathPlaceholderResolver;

	public ExternalFileBookmarkPropertiesProvider() {
		this(new PathPlaceholderResolver(Activator.getPathPlaceholdersStore()));
	}

	public ExternalFileBookmarkPropertiesProvider(PathPlaceholderResolver pathPlaceholders) {
		this.pathPlaceholderResolver = pathPlaceholders;
	}

	public void addBookmarkProperties(Map<String, String> bookmarkProperties, IWorkbenchPart part, ISelection selection,
			IProgressMonitor monitor) {
		Object selected = getFirstElement(selection);
		IPath path = AdapterUtils.getAdapter(selected, IPath.class);
		if (path == null || !path.isAbsolute() || !path.toFile().isFile()) {
			return;
		}
		putIfAbsent(bookmarkProperties, PROPERTY_NAME, () -> path.lastSegment());
		putIfAbsent(bookmarkProperties, PROP_FILE_PATH, () -> pathPlaceholderResolver.collapse(path));
	}

}
