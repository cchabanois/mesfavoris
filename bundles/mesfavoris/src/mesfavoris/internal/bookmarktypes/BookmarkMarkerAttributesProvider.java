package mesfavoris.internal.bookmarktypes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import mesfavoris.bookmarktype.BookmarkMarkerDescriptor;
import mesfavoris.bookmarktype.IBookmarkMarkerAttributesProvider;
import mesfavoris.model.Bookmark;

public class BookmarkMarkerAttributesProvider implements
		IBookmarkMarkerAttributesProvider {
	private final List<IBookmarkMarkerAttributesProvider> bookmarkMarkerAttributesProviders;

	public BookmarkMarkerAttributesProvider(
			List<IBookmarkMarkerAttributesProvider> bookmarkMarkerAttributesProviders) {
		this.bookmarkMarkerAttributesProviders = new ArrayList<IBookmarkMarkerAttributesProvider>(
				bookmarkMarkerAttributesProviders);
	}

	@Override
	public BookmarkMarkerDescriptor getMarkerDescriptor(Bookmark bookmark, IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, bookmarkMarkerAttributesProviders.size());
		for (IBookmarkMarkerAttributesProvider provider : bookmarkMarkerAttributesProviders) {
			BookmarkMarkerDescriptor bookmarkMarkerDescriptor = provider
					.getMarkerDescriptor(bookmark, subMonitor.newChild(1));
			if (bookmarkMarkerDescriptor != null) {
				return bookmarkMarkerDescriptor;
			}
		}
		return null;
	}

}
