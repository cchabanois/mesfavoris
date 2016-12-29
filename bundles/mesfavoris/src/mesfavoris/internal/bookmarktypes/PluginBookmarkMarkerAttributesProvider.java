package mesfavoris.internal.bookmarktypes;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import mesfavoris.bookmarktype.BookmarkMarkerDescriptor;
import mesfavoris.bookmarktype.IBookmarkMarkerAttributesProvider;
import mesfavoris.model.Bookmark;

public class PluginBookmarkMarkerAttributesProvider implements IBookmarkMarkerAttributesProvider {
	private final PluginBookmarkTypes pluginBookmarkTypes;
	private BookmarkMarkerAttributesProvider bookmarkMarkerAttributesProvider;

	public PluginBookmarkMarkerAttributesProvider(PluginBookmarkTypes pluginBookmarkTypes) {
		this.pluginBookmarkTypes = pluginBookmarkTypes;
	}

	private synchronized BookmarkMarkerAttributesProvider getBookmarkMarkerAttributesProvider() {
		if (bookmarkMarkerAttributesProvider != null) {
			return bookmarkMarkerAttributesProvider;
		}
		List<IBookmarkMarkerAttributesProvider> providers = pluginBookmarkTypes.getMarkerAttributesProviders();
		this.bookmarkMarkerAttributesProvider = new BookmarkMarkerAttributesProvider(providers);
		return bookmarkMarkerAttributesProvider;
	}

	@Override
	public BookmarkMarkerDescriptor getMarkerDescriptor(Bookmark bookmark, IProgressMonitor monitor) {
		return getBookmarkMarkerAttributesProvider().getMarkerDescriptor(bookmark, monitor);
	}

}
