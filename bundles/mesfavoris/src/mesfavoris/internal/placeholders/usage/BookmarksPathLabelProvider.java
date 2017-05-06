package mesfavoris.internal.placeholders.usage;

import java.util.List;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
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

import mesfavoris.bookmarktype.IBookmarkLabelProvider;
import mesfavoris.commons.ui.viewers.StylerProvider;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.remote.IRemoteBookmarksStore;
import mesfavoris.remote.IRemoteBookmarksStore.State;
import mesfavoris.remote.RemoteBookmarkFolder;
import mesfavoris.remote.RemoteBookmarksStoreManager;

/**
 * Label provider for bookmarks displaying the path
 * 
 * @author cchabanois
 *
 */
public class BookmarksPathLabelProvider extends StyledCellLabelProvider implements ILabelProvider, IStyledLabelProvider {
	private final BookmarkDatabase bookmarkDatabase;
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;
	private final IBookmarkLabelProvider bookmarkLabelProvider;
	private final ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());
	private final Color commentColor;
	private final StylerProvider stylerProvider = new StylerProvider();
	private final Color disabledColor;
	private final List<String> pathPropertyNames;
	
	public BookmarksPathLabelProvider(BookmarkDatabase bookmarkDatabase,
			RemoteBookmarksStoreManager remoteBookmarksStoreManager, IBookmarkLabelProvider bookmarkLabelProvider, List<String> pathPropertyNames) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.remoteBookmarksStoreManager = remoteBookmarksStoreManager;
		this.bookmarkLabelProvider = bookmarkLabelProvider;
		this.pathPropertyNames = pathPropertyNames;
		this.disabledColor = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_GRAY);
		this.commentColor = new Color(PlatformUI.getWorkbench().getDisplay(), 63, 127, 95);
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
		String path = getPath(bookmark);
		boolean isDisabled = isUnderDisconnectedRemoteBookmarkFolder(bookmark);
		StyledString styledString = new StyledString();
		styledString.append(bookmarkLabelProvider.getStyledText(bookmark));
		if (isDisabled) {
			Color color = null;
			Font font = null;
			if (isDisabled) {
				color = disabledColor;
			}
			styledString.setStyle(0, styledString.length(), stylerProvider.getStyler(font, color, null));
		}

		if (path != null) {
			Color color = commentColor;
			Font font = null;
			if (isDisabled) {
				color = disabledColor;
			}
			styledString.append(" - " + path, stylerProvider.getStyler(font, color, null));
		}
		return styledString;
	}

	private String getPath(Bookmark bookmark) {
		for (String pathPropertyName : pathPropertyNames) {
			String path = bookmark.getPropertyValue(pathPropertyName);
			if (path != null) {
				return path;
			}
		}
		return null;
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
		bookmarkLabelProvider.dispose();
		resourceManager.dispose();
		commentColor.dispose();
	}

	@Override
	public Image getImage(final Object element) {
		Bookmark bookmark = (Bookmark) Adapters.adapt(element, Bookmark.class);
		Image image = bookmarkLabelProvider.getImage(bookmark);
		if (image == null) {
			String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			image = PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		}
		ImageDescriptor[] overlayImages = getOverlayImages(element);
		DecorationOverlayIcon decorated = new DecorationOverlayIcon(image, overlayImages);
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