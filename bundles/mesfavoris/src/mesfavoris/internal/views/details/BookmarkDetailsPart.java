package mesfavoris.internal.views.details;

import java.util.IdentityHashMap;
import java.util.List;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;

import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.StatusHelper;
import mesfavoris.internal.views.ProxySelectionProvider;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.ui.details.IBookmarkDetailPart;

/**
 * Tab folder for bookmark details (comments ...)
 * 
 * @author cchabanois
 */
public class BookmarkDetailsPart implements IBookmarkDetailPart {
	private final List<IBookmarkDetailPart> bookmarkDetailParts;
	private final BookmarkDatabase bookmarkDatabase;
	private CTabFolder tabFolder;
	private final ProxySelectionProvider proxySelectionProvider = new ProxySelectionProvider();
	private final IdentityHashMap<CTabItem, IBookmarkDetailPart> tabItem2BookmarkDetailPart = new IdentityHashMap<>();
	
	public BookmarkDetailsPart(List<IBookmarkDetailPart> bookmarkDetailParts) {
		this.bookmarkDetailParts = bookmarkDetailParts;
		this.bookmarkDatabase = BookmarksPlugin.getDefault().getBookmarkDatabase();
	}
	
	@Override
	public void createControl(Composite parent, FormToolkit formToolkit) {
		this.tabFolder = new CTabFolder(parent, SWT.FLAT|SWT.TOP);
		formToolkit.adapt(tabFolder, true, true);	
		for (IBookmarkDetailPart bookmarkDetailPart : bookmarkDetailParts) {
			createControl(tabFolder, formToolkit, bookmarkDetailPart);
		}
	}

	private CTabItem createTabItem(CTabFolder parent, IBookmarkDetailPart bookmarkDetailPart) {
		CTabItem item = new CTabItem(parent, SWT.NULL);
		item.setText(bookmarkDetailPart.getTitle());
		item.setControl(bookmarkDetailPart.getControl().getParent());
		return item;
	}
	
	private void createControl(CTabFolder parent, FormToolkit formToolkit, IBookmarkDetailPart bookmarkDetailPart) {
		Composite composite =  formToolkit.createComposite(parent);
		GridLayoutFactory.fillDefaults().extendedMargins(3, 3, 3, 3).applyTo(composite);
		
		bookmarkDetailPart.createControl(composite, formToolkit);
		Control control = bookmarkDetailPart.getControl();
		GridDataFactory.fillDefaults().grab(true, true).applyTo(control);
		control.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				proxySelectionProvider.setCurrentSelectionProvider(null);

			}

			@Override
			public void focusGained(FocusEvent e) {
				proxySelectionProvider.setCurrentSelectionProvider(bookmarkDetailPart.getSelectionProvider());
			}
		});
	}
	
	@Override
	public Control getControl() {
		return tabFolder;
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return proxySelectionProvider;
	}

	@Override
	public void setBookmark(Bookmark bookmark) {
		if (bookmark != null && bookmarkDatabase.getBookmarksTree().getBookmark(bookmark.getId()) == null) {
			bookmark = null;
		}
		IBookmarkDetailPart previouslySelectedBookmarkDetailsPart = tabItem2BookmarkDetailPart.get(tabFolder.getSelection());
		for (CTabItem tabItem : tabFolder.getItems()) {
			tabItem.dispose();
		}
		tabItem2BookmarkDetailPart.clear();
		CTabItem selectedTabItem = null;
		for (IBookmarkDetailPart bookmarkDetailPart : bookmarkDetailParts) {
			if (bookmarkDetailPart.canHandle(bookmark)) {
				CTabItem tabItem = createTabItem(tabFolder, bookmarkDetailPart);
				tabItem2BookmarkDetailPart.put(tabItem, bookmarkDetailPart);
				bookmarkDetailPart.setBookmark(bookmark);
				if (previouslySelectedBookmarkDetailsPart == bookmarkDetailPart) {
					selectedTabItem = tabItem;
				}
			}
		}
		if (selectedTabItem == null) {
			tabFolder.setSelection(0);
		} else {
			tabFolder.setSelection(selectedTabItem);	
		}
	}
	
	@Override
	public boolean canHandle(Bookmark bookmark) {
		return true;
	}

	@Override
	public void dispose() {
		for (IBookmarkDetailPart bookmarkDetailPart : bookmarkDetailParts) {
			SafeRunner.run(new ISafeRunnable() {

				public void run() throws Exception {
					bookmarkDetailPart.dispose();
				}

				public void handleException(Throwable exception) {
					StatusHelper.logError("Error while disposing bookmarkDetailPart", exception);
				}
			});

		}
	}

	@Override
	public String getTitle() {
		return "Bookmark Details";
	}
	
}
