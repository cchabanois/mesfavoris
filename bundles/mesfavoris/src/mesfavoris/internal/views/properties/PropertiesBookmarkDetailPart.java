package mesfavoris.internal.views.properties;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.osgi.service.event.EventHandler;

import mesfavoris.internal.views.details.AbstractBookmarkDetailPart;
import mesfavoris.model.Bookmark;
import mesfavoris.topics.BookmarksEvents;

public class PropertiesBookmarkDetailPart extends AbstractBookmarkDetailPart {
	private final PropertySheetPage propertySheetPage;
	private final IEventBroker eventBroker;
	private final EventHandler bookmarkProblemsEventHandler;
	
	public PropertiesBookmarkDetailPart() {
		this.propertySheetPage = new PropertySheetPage();
		this.eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);
		bookmarkProblemsEventHandler = (event)->{
			if (!canHandle(bookmark) || bookmark == null) {
				return;
			}
			Display.getDefault().asyncExec(() -> setBookmark(bookmark));
		};
	}
	
	@Override
	public String getTitle() {
		return "Properties";
	}

	@Override
	public void createControl(Composite parent, FormToolkit formToolkit) {
		super.createControl(parent, formToolkit);
		propertySheetPage.createControl(parent);
		resizeTreeColumns();
		eventBroker.subscribe(BookmarksEvents.TOPIC_BOOKMARK_PROBLEMS_CHANGED, bookmarkProblemsEventHandler);
	}

	private void resizeTreeColumns() {
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
		super.setBookmark(bookmark);
		ISelection selection;
		if (bookmark == null) {
			selection = new StructuredSelection();
		} else {
			selection = new StructuredSelection(bookmark);
		}
		propertySheetPage.selectionChanged(null, selection);
	}

	@Override
	public boolean canHandle(Bookmark bookmark) {
		return true;
	}

	@Override
	public void dispose() {
		eventBroker.unsubscribe(bookmarkProblemsEventHandler);
		super.dispose();
		propertySheetPage.dispose();
	}

	@Override
	protected void bookmarkModified(Bookmark oldBookmark, Bookmark newBookmark) {
		Display.getDefault().asyncExec(() -> setBookmark(newBookmark));
	}

}
