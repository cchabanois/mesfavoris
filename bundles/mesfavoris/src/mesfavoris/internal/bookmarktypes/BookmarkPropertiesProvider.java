package mesfavoris.internal.bookmarktypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;

public class BookmarkPropertiesProvider implements IBookmarkPropertiesProvider {
	private final List<IBookmarkPropertiesProvider> providers;

	public BookmarkPropertiesProvider(List<IBookmarkPropertiesProvider> providers) {
		this.providers = new ArrayList<IBookmarkPropertiesProvider>(providers);
	}

	@Override
	public void addBookmarkProperties(Map<String, String> bookmarkProperties, Object selected) {
		providers.forEach(p -> p.addBookmarkProperties(bookmarkProperties, selected));
	}

}
