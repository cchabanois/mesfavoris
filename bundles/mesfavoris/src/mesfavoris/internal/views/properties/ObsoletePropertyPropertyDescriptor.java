package mesfavoris.internal.views.properties;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import mesfavoris.internal.views.properties.PropertyLabelProvider.PropertyIcon;
import mesfavoris.problems.BookmarkProblem;
import mesfavoris.problems.IBookmarkProblemDescriptor;
import mesfavoris.problems.IBookmarkProblemDescriptor.Severity;

public class ObsoletePropertyPropertyDescriptor extends PropertyDescriptor {
	private final BookmarkProblem bookmarkProblem;
	private final String propertyName;

	public ObsoletePropertyPropertyDescriptor(BookmarkProblem bookmarkProblem,
			IBookmarkProblemDescriptor bookmarkProblemDescriptor, String propertyName) {
		super(propertyName, propertyName);
		this.bookmarkProblem = bookmarkProblem;
		this.propertyName = propertyName;
		Severity severity = bookmarkProblemDescriptor.getSeverity();
		
		if (severity == Severity.ERROR || severity == Severity.WARNING) {
			setLabelProvider(new PropertyLabelProvider(false, PropertyIcon.WARNING));
		} else {
			setLabelProvider(new PropertyLabelProvider(false, PropertyIcon.INFO));
		}
	}

	@Override
	public CellEditor createPropertyEditor(Composite parent) {
		return new UpdatePropertyCellEditor(parent, bookmarkProblem, propertyName);
	}

}
