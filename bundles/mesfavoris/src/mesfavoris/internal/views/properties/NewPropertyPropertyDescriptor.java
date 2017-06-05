package mesfavoris.internal.views.properties;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import mesfavoris.internal.views.properties.PropertyLabelProvider.PropertyIcon;
import mesfavoris.problems.BookmarkProblem;
import mesfavoris.problems.IBookmarkProblemDescriptor;
import mesfavoris.problems.IBookmarkProblemDescriptor.Severity;

public class NewPropertyPropertyDescriptor extends PropertyDescriptor {
	private final BookmarkProblem bookmarkProblem;
	private final String propertyName;

	public NewPropertyPropertyDescriptor(BookmarkProblem bookmarkProblem,
			IBookmarkProblemDescriptor bookmarkProblemDescriptor, String propertyName) {
		super(propertyName, propertyName + " (New value)");
		this.bookmarkProblem = bookmarkProblem;
		this.propertyName = propertyName;
		Severity severity = bookmarkProblemDescriptor.getSeverity();
		
		if (severity == Severity.ERROR || severity == Severity.WARNING) {
			setLabelProvider(new PropertyLabelProvider(true, PropertyIcon.WARNING));
		} else {
			setLabelProvider(new PropertyLabelProvider(true, PropertyIcon.INFO));
		}
	}

	@Override
	public CellEditor createPropertyEditor(Composite parent) {
		return new UpdatePropertyCellEditor(parent, bookmarkProblem, propertyName);
	}

}
