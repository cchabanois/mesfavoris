package mesfavoris.internal.bookmarktypes;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;

public class PluginBookmarkPropertiesProvider implements IBookmarkPropertiesProvider {
	private final PluginBookmarkTypes pluginBookmarkTypes;
	private BookmarkPropertiesProvider bookmarkPropertiesProvider;

	public PluginBookmarkPropertiesProvider(PluginBookmarkTypes pluginBookmarkTypes) {
		this.pluginBookmarkTypes = pluginBookmarkTypes;
	}

	private synchronized BookmarkPropertiesProvider getBookmarkPropertiesProvider() {
		if (bookmarkPropertiesProvider != null) {
			return bookmarkPropertiesProvider;
		}
		List<IBookmarkPropertiesProvider> providers = pluginBookmarkTypes.getPropertiesProviders();
		this.bookmarkPropertiesProvider = new BookmarkPropertiesProvider(providers);
		return bookmarkPropertiesProvider;
	}

	@Override
	public void addBookmarkProperties(Map<String, String> bookmarkProperties, IWorkbenchPart part, ISelection selection,
			IProgressMonitor monitor) {
		getBookmarkPropertiesProvider().addBookmarkProperties(bookmarkProperties, part, selection, monitor);
	}

}
