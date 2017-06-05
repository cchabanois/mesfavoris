package mesfavoris.internal.views.properties;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.problems.BookmarkProblem;

public class UpdatePropertyCellEditor extends DialogCellEditor {
	private Image image;
	private final BookmarkProblem bookmarkProblem;
	private final String propertyName;

	public UpdatePropertyCellEditor(Composite parent, BookmarkProblem bookmarkProblem, String propertyName) {
		super(parent);
		this.bookmarkProblem = bookmarkProblem;
		this.propertyName = propertyName;
	}

	@Override
	protected Button createButton(Composite parent) {
		Button result = new Button(parent, SWT.FLAT);
		if (image == null) {
			image = BookmarksPlugin.getImageDescriptor("icons/bookmark-update.png").createImage();
		}
		result.addPaintListener(event -> event.gc.drawImage(image, 0, 0));
		return result;
	}

	@Override
	protected Object openDialogBox(Control cellEditorWindow) {
		return bookmarkProblem.getProperties().get(propertyName);
	}

	@Override
	public void dispose() {
		if (image != null) {
			image.dispose();
		}
		super.dispose();
	}

}
