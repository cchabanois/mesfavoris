package mesfavoris.internal.bookmarktypes.extension;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;

import mesfavoris.bookmarktype.IBookmarkLabelProvider;
import mesfavoris.internal.bookmarktypes.BookmarkLabelProvider;
import mesfavoris.model.Bookmark;

public class PluginBookmarkLabelProvider implements
		IBookmarkLabelProvider {
	private final PluginBookmarkTypes pluginBookmarkTypes;
	private BookmarkLabelProvider bookmarkLabelProvider;

	public PluginBookmarkLabelProvider(PluginBookmarkTypes pluginBookmarkTypes) {
		this.pluginBookmarkTypes = pluginBookmarkTypes;
	}

	private synchronized BookmarkLabelProvider getBookmarkLabelProvider() {
		if (bookmarkLabelProvider != null) {
			return bookmarkLabelProvider;
		}
		List<IBookmarkLabelProvider> labelProviders = pluginBookmarkTypes.getLabelProviders();
		this.bookmarkLabelProvider = new BookmarkLabelProvider(labelProviders);
		return bookmarkLabelProvider;
	}

	@Override
	public ImageDescriptor getImageDescriptor(Context context, Bookmark bookmark) {
		return getBookmarkLabelProvider().getImageDescriptor(context, bookmark);
	}

	@Override
	public StyledString getStyledText(Context context, Bookmark bookmark) {
		return getBookmarkLabelProvider().getStyledText(context, bookmark);
	}

	@Override
	public boolean canHandle(Context context, Bookmark bookmark) {
		return getBookmarkLabelProvider().canHandle(context, bookmark);
	}

}
