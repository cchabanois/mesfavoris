package org.chabanois.mesfavoris.internal.views.properties;

import java.util.Set;

import org.chabanois.mesfavoris.model.Bookmark;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class BookmarkPropertySource implements IPropertySource {
	private final Bookmark bookmark;

	public BookmarkPropertySource(Bookmark bookmark) {
		this.bookmark = bookmark;
	}

	@Override
	public Object getEditableValue() {
		return bookmark;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		Set<String> propertyNames = bookmark.getProperties().keySet();
		IPropertyDescriptor[] propertyDescriptors = new IPropertyDescriptor[propertyNames
				.size()];
		int i = 0;
		for (String propertyName : propertyNames) {
			PropertyDescriptor propertyDescriptor = new PropertyDescriptor(
					propertyName, propertyName);
			propertyDescriptors[i] = propertyDescriptor;
			i++;
		}
		return propertyDescriptors;
	}

	@Override
	public Object getPropertyValue(Object id) {
		return bookmark.getPropertyValue((String)id);
	}

	@Override
	public boolean isPropertySet(Object id) {
		return false;
	}

	@Override
	public void resetPropertyValue(Object id) {

	}

	@Override
	public void setPropertyValue(Object id, Object value) {

	}

}
