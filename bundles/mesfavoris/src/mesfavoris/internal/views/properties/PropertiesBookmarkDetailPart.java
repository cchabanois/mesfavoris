package mesfavoris.internal.views.properties;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.views.properties.PropertySheetPage;

import mesfavoris.internal.views.BookmarksView;
import mesfavoris.model.Bookmark;
import mesfavoris.ui.details.IBookmarkDetailPart;

public class PropertiesBookmarkDetailPart implements IBookmarkDetailPart {
	private PropertySheetPage propertySheetPage;

	@Override
	public String getTitle() {
		return "Properties";
	}

	@Override
	public void initialize(BookmarksView bookmarksView) {
		propertySheetPage = new PropertySheetPage();

	}

	@Override
	public void createControl(Composite parent) {
		propertySheetPage.createControl(parent);
		Tree tree = (Tree) propertySheetPage.getControl();
		tree.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				Rectangle area = tree.getClientArea();
				TreeColumn[] columns = tree.getColumns();
				if (columns[0].getWidth() == 0) {
					columns[0].setWidth(area.width * 40 / 100);
					columns[1].setWidth(area.width - columns[0].getWidth() - 4);
				}
				if (columns[0].getWidth() > 0) {
					tree.removeControlListener(this);
				}
			}
		});

	}

	@Override
	public Control getControl() {
		return propertySheetPage.getControl();
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return null;
	}

	@Override
	public void setBookmark(Bookmark bookmark) {
		propertySheetPage.selectionChanged(null, new StructuredSelection(bookmark));
	}

	@Override
	public boolean canHandle(Bookmark bookmark) {
		return true;
	}

	@Override
	public void dispose() {
		propertySheetPage.dispose();
	}

}
