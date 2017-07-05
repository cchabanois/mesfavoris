package mesfavoris.viewers;

import java.util.NoSuchElementException;
import java.util.Scanner;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import mesfavoris.bookmarktype.BookmarkDatabaseLabelProviderContext;
import mesfavoris.bookmarktype.IBookmarkLabelProvider;
import mesfavoris.bookmarktype.IBookmarkLabelProvider.Context;
import mesfavoris.commons.ui.jface.OverlayIconImageDescriptor;
import mesfavoris.commons.ui.viewers.StylerProvider;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.remote.IRemoteBookmarksStore;
import mesfavoris.remote.IRemoteBookmarksStore.State;
import mesfavoris.remote.RemoteBookmarkFolder;
import mesfavoris.remote.RemoteBookmarksStoreManager;

/**
 * Label provider for bookmarks
 * 
 * @author cchabanois
 *
 */
public class BookmarksTableLabelProvider extends StyledCellLabelProvider implements ILabelProvider, IStyledLabelProvider {
	private final BookmarkDatabase bookmarkDatabase;
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;
	private final IBookmarkLabelProvider bookmarkLabelProvider;
	private final ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());
	private final Color commentColor;
	private final StylerProvider stylerProvider = new StylerProvider();
	private final Color disabledColor;
	private final Context context;
	
	public BookmarksTableLabelProvider(BookmarkDatabase bookmarkDatabase,
			RemoteBookmarksStoreManager remoteBookmarksStoreManager, IBookmarkLabelProvider bookmarkLabelProvider) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.remoteBookmarksStoreManager = remoteBookmarksStoreManager;
		this.bookmarkLabelProvider = bookmarkLabelProvider;
		this.disabledColor = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_GRAY);
		this.commentColor = new Color(PlatformUI.getWorkbench().getDisplay(), 63, 127, 95);
		this.context = new BookmarkDatabaseLabelProviderContext(bookmarkDatabase);
	}

	@Override
	public void update(ViewerCell cell) {
		StyledString styledText = getStyledText(cell.getElement());
		cell.setText(styledText.toString());
		cell.setStyleRanges(styledText.getStyleRanges());
		cell.setImage(getImage(cell.getElement()));
		super.update(cell);
	}

	public StyledString getStyledText(final Object element) {
		Bookmark bookmark = (Bookmark) Adapters.adapt(element, Bookmark.class);
		String comment = getFirstCommentLine(bookmark);
		boolean isDisabled = isUnderDisconnectedRemoteBookmarkFolder(bookmark);
		StyledString styledString = new StyledString();
		styledString.append(bookmarkLabelProvider.getStyledText(context, bookmark));
		if (isDisabled) {
			Color color = null;
			Font font = null;
			if (isDisabled) {
				color = disabledColor;
			}
			styledString.setStyle(0, styledString.length(), stylerProvider.getStyler(font, color, null));
		}

		if (comment != null) {
			Color color = commentColor;
			Font font = null;
			if (isDisabled) {
				color = disabledColor;
			}
			styledString.append(" - " + comment, stylerProvider.getStyler(font, color, null));
		}
		return styledString;
	}
	
	private String getFirstCommentLine(Bookmark bookmark) {
		String comment = bookmark.getPropertyValue(Bookmark.PROPERTY_COMMENT);
		if (comment == null) {
			return null;
		}
		try (Scanner scanner = new Scanner(comment)) {
			return scanner.nextLine();
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	private boolean isUnderDisconnectedRemoteBookmarkFolder(Bookmark bookmark) {
		RemoteBookmarkFolder remoteBookmarkFolder = remoteBookmarksStoreManager
				.getRemoteBookmarkFolderContaining(bookmarkDatabase.getBookmarksTree(), bookmark.getId());
		if (remoteBookmarkFolder == null) {
			return false;
		}
		IRemoteBookmarksStore remoteBookmarksStore = remoteBookmarksStoreManager
				.getRemoteBookmarksStore(remoteBookmarkFolder.getRemoteBookmarkStoreId());
		if (remoteBookmarksStore.getState() != State.connected) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		resourceManager.dispose();
		commentColor.dispose();
	}

	@Override
	public Image getImage(final Object element) {
		Bookmark bookmark = (Bookmark) Adapters.adapt(element, Bookmark.class);
		ImageDescriptor imageDescriptor = bookmarkLabelProvider.getImageDescriptor(context, bookmark);
		if (imageDescriptor == null) {
			String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			imageDescriptor = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(imageKey);
		}
		ImageDescriptor[] overlayImages = getOverlayImages(element);
		OverlayIconImageDescriptor decorated = new OverlayIconImageDescriptor(imageDescriptor, overlayImages, 16, 16);
		return (Image) this.resourceManager.get(decorated);
	}

	private ImageDescriptor[] getOverlayImages(final Object element) {
		Bookmark bookmark = (Bookmark) Adapters.adapt(element, Bookmark.class);
		ImageDescriptor[] overlayImages = new ImageDescriptor[5];
		RemoteBookmarkFolder remoteBookmarkFolder = remoteBookmarksStoreManager
				.getRemoteBookmarkFolderContaining(bookmarkDatabase.getBookmarksTree(), bookmark.getId());
		if (remoteBookmarkFolder != null) {
			IRemoteBookmarksStore remoteBookmarksStore = remoteBookmarksStoreManager
					.getRemoteBookmarksStore(remoteBookmarkFolder.getRemoteBookmarkStoreId());
			overlayImages[IDecoration.TOP_RIGHT] = remoteBookmarksStore.getDescriptor().getImageOverlayDescriptor();
		}
		return overlayImages;
	}

	@Override
	public String getText(Object element) {
		return getStyledText(element).toString();
	}

}