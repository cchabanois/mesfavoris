package mesfavoris.internal.views.properties;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import mesfavoris.internal.views.properties.PropertyLabelProvider.PropertyIcon;

public class ObsoletePropertyPropertySource implements IPropertySource {
	private final String propertyName;
	private final String updatedValue;
	private final String propertyValue;

	public ObsoletePropertyPropertySource(String propertyName, String propertyValue, String updatedValue) {
		this.propertyName = propertyName;
		this.updatedValue = updatedValue;
		this.propertyValue = propertyValue;
	}

	@Override
	public Object getEditableValue() {
		return propertyValue;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		PropertyDescriptor propertyDescriptor = new PropertyDescriptor(propertyName, "Updated value");
		propertyDescriptor.setLabelProvider(new PropertyLabelProvider(true, PropertyIcon.NONE));
		return new IPropertyDescriptor[] { propertyDescriptor };
	}

	@Override
	public Object getPropertyValue(Object id) {
		return updatedValue;
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