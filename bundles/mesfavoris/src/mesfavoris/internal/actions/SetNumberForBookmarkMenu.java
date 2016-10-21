package mesfavoris.internal.actions;

import java.util.Optional;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

import mesfavoris.BookmarksPlugin;
import mesfavoris.bookmarktype.IBookmarkLabelProvider;
import mesfavoris.internal.numberedbookmarks.BookmarkNumber;
import mesfavoris.internal.numberedbookmarks.NumberedBookmarkDecorationProvider;
import mesfavoris.internal.numberedbookmarks.NumberedBookmarks;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;
import mesfavoris.service.IBookmarksService;

public class SetNumberForBookmarkMenu extends ContributionItem implements IWorkbenchContribution {
	private final NumberedBookmarks numberedBookmarks;
	private final ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());
	private final IBookmarkLabelProvider bookmarkLabelProvider;
	private final IBookmarksService bookmarksService;

	private IServiceLocator serviceLocator;

	public SetNumberForBookmarkMenu() {
		this.numberedBookmarks = BookmarksPlugin.getNumberedBookmarks();
		this.bookmarkLabelProvider = BookmarksPlugin.getBookmarkLabelProvider();
		this.bookmarksService = BookmarksPlugin.getBookmarksService();
	}

	@Override
	public void initialize(IServiceLocator serviceLocator) {
		this.serviceLocator = serviceLocator;
	}

	@Override
	public void dispose() {
		resourceManager.dispose();
	}

	@Override
	public void fill(Menu menu, int index) {
		BookmarkId bookmarkId = getSelectedBookmarkId();
		createMenuItem(menu, bookmarkId, BookmarkNumber.ONE);
		createMenuItem(menu, bookmarkId, BookmarkNumber.TWO);
		createMenuItem(menu, bookmarkId, BookmarkNumber.THREE);
		createMenuItem(menu, bookmarkId, BookmarkNumber.FOUR);
		createMenuItem(menu, bookmarkId, BookmarkNumber.FIVE);
		createMenuItem(menu, bookmarkId, BookmarkNumber.SIX);
		createMenuItem(menu, bookmarkId, BookmarkNumber.SEVEN);
		createMenuItem(menu, bookmarkId, BookmarkNumber.EIGHT);
		createMenuItem(menu, bookmarkId, BookmarkNumber.NINE);
		createMenuItem(menu, bookmarkId, BookmarkNumber.ZERO);
	}

	private BookmarkId getSelectedBookmarkId() {
		ISelectionService selectionService = serviceLocator.getService(ISelectionService.class);
		if (selectionService == null || !(selectionService.getSelection() instanceof IStructuredSelection)) {
			return null;
		}
		IStructuredSelection selection = (IStructuredSelection) selectionService.getSelection();
		Object first = selection.getFirstElement();
		if (!(first instanceof Bookmark)) {
			return null;
		}
		return ((Bookmark) first).getId();
	}

	private void createMenuItem(Menu menu, BookmarkId selectedBookmarkId, BookmarkNumber bookmarkNumber) {
		Optional<BookmarkId> bookmarkId = numberedBookmarks.getBookmark(bookmarkNumber);
		final MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
		menuItem.setSelection(bookmarkId.isPresent() ? bookmarkId.get().equals(selectedBookmarkId) : false);
		menuItem.setText(getText(bookmarkId));
		menuItem.setAccelerator('0'+bookmarkNumber.getNumber());
		Image image = getImage(bookmarkNumber, bookmarkId);
		if (image != null) {
			menuItem.setImage(image);
		}
		Listener listener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				switch (event.type) {
				case SWT.Selection:
					if (menuItem.getSelection()) {
						bookmarksService.addNumberedBookmark(selectedBookmarkId, bookmarkNumber);
					}
					break;
				}
			}
		};
		menuItem.addListener(SWT.Selection, listener);
	}

	private String getText(Optional<BookmarkId> bookmarkId) {
		if (!bookmarkId.isPresent()) {
			return "Empty";
		}
		Bookmark bookmark = bookmarksService.getBookmarksTree().getBookmark(bookmarkId.get());
		if (bookmark == null) {
			return "Empty";
		}
		return bookmarkLabelProvider.getStyledText(bookmark).toString();
	}

	private Image getImage(BookmarkNumber bookmarkNumber, Optional<BookmarkId> bookmarkId) {
		ImageDescriptor numberImageDescriptor = NumberedBookmarkDecorationProvider.getImageDescriptor(bookmarkNumber);
		if (!bookmarkId.isPresent()) {
			return (Image) this.resourceManager.get(numberImageDescriptor);
		}
		Bookmark bookmark = bookmarksService.getBookmarksTree().getBookmark(bookmarkId.get());
		if (bookmark == null) {
			return (Image) this.resourceManager.get(numberImageDescriptor);
		}
		Image image = bookmarkLabelProvider.getImage(bookmark);
		ImageDescriptor[] overlayImages = new ImageDescriptor[5];
		overlayImages[IDecoration.TOP_LEFT] = numberImageDescriptor;
		DecorationOverlayIcon decorated = new DecorationOverlayIcon(image, overlayImages);
		return (Image) this.resourceManager.get(decorated);
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

}
