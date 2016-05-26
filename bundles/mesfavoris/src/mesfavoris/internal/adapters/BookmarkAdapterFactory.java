package mesfavoris.internal.adapters;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.views.properties.IPropertySource;

import mesfavoris.internal.views.properties.BookmarkPropertySource;
import mesfavoris.model.Bookmark;

public class BookmarkAdapterFactory implements IAdapterFactory {

	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (!(adaptableObject instanceof Bookmark)) {
			return null;
		}
		Bookmark bookmark = (Bookmark)adaptableObject;
		if (IPropertySource.class.equals(adapterType)) {
			return new BookmarkPropertySource(bookmark);
		}
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return new Class[] { IPropertySource.class };
	}

}
