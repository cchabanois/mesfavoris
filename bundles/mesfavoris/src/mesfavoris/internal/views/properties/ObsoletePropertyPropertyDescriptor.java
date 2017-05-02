package mesfavoris.internal.views.properties;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import mesfavoris.internal.views.properties.PropertyLabelProvider.PropertyIcon;
import mesfavoris.problems.BookmarkProblem;

public class ObsoletePropertyPropertyDescriptor extends PropertyDescriptor {
	private final BookmarkProblem bookmarkProblem;
	private final String propertyName;
	
	public ObsoletePropertyPropertyDescriptor(BookmarkProblem bookmarkProblem, String propertyName) {
		super(propertyName, propertyName);
		this.bookmarkProblem = bookmarkProblem;
		this.propertyName = propertyName;
		if (BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE.equals(bookmarkProblem.getProblemType())) {
			setLabelProvider(new PropertyLabelProvider(false, PropertyIcon.WARNING));
		} else if (BookmarkProblem.TYPE_PROPERTIES_MAY_UPDATE.equals(bookmarkProblem.getProblemType())) {
			setLabelProvider(new PropertyLabelProvider(false, PropertyIcon.INFO));
		}
	}

	@Override
	public CellEditor createPropertyEditor(Composite parent) {
		return new ObsoletePropertyCellEditor(parent, bookmarkProblem, propertyName);
	}
	
}
