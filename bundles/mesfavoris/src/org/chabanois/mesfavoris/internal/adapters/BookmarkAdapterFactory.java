package org.chabanois.mesfavoris.internal.adapters;

import org.chabanois.mesfavoris.internal.views.properties.BookmarkPropertySource;
import org.chabanois.mesfavoris.model.Bookmark;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.views.properties.IPropertySource;

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
