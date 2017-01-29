package mesfavoris.viewers;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;

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
import mesfavoris.commons.core.AdapterUtils;
import mesfavoris.commons.ui.viewers.StylerProvider;
import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.numberedbookmarks.BookmarkNumber;
import mesfavoris.internal.numberedbookmarks.NumberedBookmarks;
import mesfavoris.internal.numberedbookmarks.NumberedBookmarksImageDescriptors;
import mesfavoris.internal.views.virtual.BookmarkLink;
import mesfavoris.internal.views.virtual.VirtualBookmarkFolder;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.persistence.IDirtyBookmarksProvider;
import mesfavoris.problems.BookmarkProblem.Severity;
import mesfavoris.problems.IBookmarkProblems;
import mesfavoris.remote.IRemoteBookmarksStore;
import mesfavoris.remote.IRemoteBookmarksStore.State;
import mesfavoris.remote.RemoteBookmarkFolder;
import mesfavoris.remote.RemoteBookmarksStoreManager;

public class BookmarksLabelProvider extends StyledCellLabelProvider implements ILabelProvider, IStyledLabelProvider {
	private static final String ICON_VIRTUAL_BOOKMARK_FOLDER = "icons/ovr16/virt_ovr.png";
	private static final String ICON_BOOKMARK_LINK = "icons/ovr16/link_ovr.png";
	private static final String ICON_ERROR = "icons/ovr16/error.png";
	private final BookmarkDatabase bookmarkDatabase;
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;
	private final IBookmarkLabelProvider bookmarkLabelProvider;
	private final NumberedBookmarks numberedBookmarks;
	private final ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());
	private final Color commentColor;
	private final StylerProvider stylerProvider = new StylerProvider();
	private final Color disabledColor;
	private final IDirtyBookmarksProvider dirtyBookmarksProvider;
	private final IBookmarkProblems bookmarkProblems;

	public BookmarksLabelProvider(BookmarkDatabase bookmarkDatabase,
			RemoteBookmarksStoreManager remoteBookmarksStoreManager, IDirtyBookmarksProvider dirtyBookmarksProvider,
			IBookmarkLabelProvider bookmarkLabelProvider, NumberedBookmarks numberedBookmarks,
			IBookmarkProblems bookmarkProblems) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.remoteBookmarksStoreManager = remoteBookmarksStoreManager;
		this.bookmarkLabelProvider = bookmarkLabelProvider;
		this.dirtyBookmarksProvider = dirtyBookmarksProvider;
		this.numberedBookmarks = numberedBookmarks;
		this.bookmarkProblems = bookmarkProblems;
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
		Bookmark bookmark = (Bookmark) AdapterUtils.getAdapter(element, Bookmark.class);
		String comment = getFirstCommentLine(bookmark);
		boolean hasComment = comment != null && comment.trim().length() > 0;
		boolean isDisabled = isUnderDisconnectedRemoteBookmarkFolder(bookmark);
		StyledString styledString = new StyledString();
		if (dirtyBookmarksProvider.getDirtyBookmarks().contains(bookmark.getId())) {
			styledString.append("> ");
		}
		styledString.append(bookmarkLabelProvider.getStyledText(bookmark));
		if (isDisabled) {
			Color color = null;
			Font font = null;
			if (isDisabled) {
				color = disabledColor;
			}
			styledString.setStyle(0, styledString.length(), stylerProvider.getStyler(font, color, null));
		}

		if (hasComment) {
			Color color = commentColor;
			Font font = null;
			if (isDisabled) {
				color = disabledColor;
			}
			styledString.append(" - " + comment, stylerProvider.getStyler(font, color, null));
		}
		return styledString;
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
		Bookmark bookmark = (Bookmark) AdapterUtils.getAdapter(element, Bookmark.class);
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
		Bookmark bookmark = (Bookmark) AdapterUtils.getAdapter(element, Bookmark.class);
		ImageDescriptor[] overlayImages = new ImageDescriptor[5];
		if (bookmark instanceof BookmarkFolder) {
			IRemoteBookmarksStore store = getRemoteBookmarkStore(bookmark.getId());
			if (store != null && store.getDescriptor() != null) {
				overlayImages[IDecoration.TOP_RIGHT] = store.getDescriptor().getImageOverlayDescriptor();
			}
		}
		Optional<BookmarkNumber> bookmarkNumber = numberedBookmarks.getBookmarkNumber(bookmark.getId());
		if (bookmarkNumber.isPresent()) {
			overlayImages[IDecoration.TOP_LEFT] = NumberedBookmarksImageDescriptors
					.getImageDescriptor(bookmarkNumber.get());
		}
		if (element instanceof BookmarkLink) {
			ImageDescriptor imageDescriptor = BookmarksPlugin.getImageDescriptor(ICON_BOOKMARK_LINK);
			overlayImages[IDecoration.BOTTOM_RIGHT] = imageDescriptor;
		} else if (element instanceof VirtualBookmarkFolder) {
			ImageDescriptor imageDescriptor = BookmarksPlugin.getImageDescriptor(ICON_VIRTUAL_BOOKMARK_FOLDER);
			overlayImages[IDecoration.BOTTOM_RIGHT] = imageDescriptor;
		}
		Optional<ImageDescriptor> problemImageDescriptor = getProblemOverlayImageDescriptor(bookmark.getId());
		if (problemImageDescriptor.isPresent()) {
			overlayImages[IDecoration.BOTTOM_LEFT] = problemImageDescriptor.get();
		}
		return overlayImages;
	}

	private Optional<ImageDescriptor> getProblemOverlayImageDescriptor(BookmarkId bookmarkId) {
		return getProblemSeverity(bookmarkId).map(severity-> {
			switch (severity) {
			case ERROR : return ISharedImages.IMG_DEC_FIELD_ERROR;
			default : return ISharedImages.IMG_DEC_FIELD_WARNING;
			}
		}).map(key->PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(key));
	}
	
	private Optional<Severity> getProblemSeverity(BookmarkId bookmarkId) {
		return bookmarkProblems.getBookmarkProblems(bookmarkId).stream().map(problem -> problem.getSeverity())
				.findFirst();
	}

	private IRemoteBookmarksStore getRemoteBookmarkStore(BookmarkId bookmarkFolderId) {
		for (IRemoteBookmarksStore store : remoteBookmarksStoreManager.getRemoteBookmarksStores()) {
			if (store.getRemoteBookmarkFolder(bookmarkFolderId).isPresent()) {
				return store;
			}
		}
		return null;
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

	@Override
	public String getText(Object element) {
		return getStyledText(element).toString();
	}

}