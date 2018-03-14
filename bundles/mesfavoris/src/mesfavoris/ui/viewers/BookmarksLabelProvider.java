package mesfavoris.ui.viewers;

import java.util.NoSuchElementException;
import java.util.Optional;
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
import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.IUIConstants;
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
import mesfavoris.problems.IBookmarkProblemDescriptor.Severity;
import mesfavoris.problems.IBookmarkProblems;
import mesfavoris.remote.IRemoteBookmarksStore;
import mesfavoris.remote.IRemoteBookmarksStore.State;
import mesfavoris.remote.RemoteBookmarksStoreManager;

public class BookmarksLabelProvider extends StyledCellLabelProvider implements ILabelProvider, IStyledLabelProvider {
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
	private final Context context;

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
		boolean hasComment = comment != null && comment.trim().length() > 0;
		boolean isDisabled = isUnderDisconnectedRemoteBookmarkFolder(bookmark);
		StyledString styledString = new StyledString();
		if (dirtyBookmarksProvider.getDirtyBookmarks().contains(bookmark.getId())) {
			styledString.append("> ");
		}
		styledString.append(bookmarkLabelProvider.getStyledText(context, bookmark));
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
		return remoteBookmarksStoreManager
				.getRemoteBookmarkFolderContaining(bookmarkDatabase.getBookmarksTree(), bookmark.getId())
				.flatMap(remoteBookmarkFolder -> remoteBookmarksStoreManager
						.getRemoteBookmarksStore(remoteBookmarkFolder.getRemoteBookmarkStoreId()))
				.map(remoteBookmarksStore -> remoteBookmarksStore.getState() != State.connected).orElse(false);
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
		OverlayIconImageDescriptor decorated = new OverlayIconImageDescriptor(imageDescriptor, overlayImages);
		return (Image) this.resourceManager.get(decorated);
	}

	private ImageDescriptor[] getOverlayImages(final Object element) {
		Bookmark bookmark = (Bookmark) Adapters.adapt(element, Bookmark.class);
		ImageDescriptor[] overlayImages = new ImageDescriptor[5];
		if (bookmark instanceof BookmarkFolder) {
			getRemoteBookmarkStore(bookmark.getId()).map(store->store.getDescriptor()).ifPresent(descriptor->{
				overlayImages[IDecoration.TOP_RIGHT] = descriptor.getImageOverlayDescriptor();
			});
		}
		Optional<BookmarkNumber> bookmarkNumber = numberedBookmarks.getBookmarkNumber(bookmark.getId());
		if (bookmarkNumber.isPresent()) {
			overlayImages[IDecoration.TOP_LEFT] = NumberedBookmarksImageDescriptors
					.getImageDescriptor(bookmarkNumber.get());
		}
		if (element instanceof BookmarkLink) {
			ImageDescriptor imageDescriptor = BookmarksPlugin.getImageDescriptor(IUIConstants.IMG_BOOKMARK_LINK);
			overlayImages[IDecoration.BOTTOM_RIGHT] = imageDescriptor;
		} else if (element instanceof VirtualBookmarkFolder) {
			ImageDescriptor imageDescriptor = BookmarksPlugin
					.getImageDescriptor(IUIConstants.IMG_VIRTUAL_BOOKMARK_FOLDER);
			overlayImages[IDecoration.BOTTOM_RIGHT] = imageDescriptor;
		}
		Optional<ImageDescriptor> problemImageDescriptor = getProblemOverlayImageDescriptor(bookmark.getId());
		if (problemImageDescriptor.isPresent()) {
			overlayImages[IDecoration.BOTTOM_LEFT] = problemImageDescriptor.get();
		}
		return overlayImages;
	}

	private Optional<ImageDescriptor> getProblemOverlayImageDescriptor(BookmarkId bookmarkId) {
		return getProblemSeverity(bookmarkId).flatMap(severity -> {
			switch (severity) {
			case WARNING:
				return Optional.of(ISharedImages.IMG_DEC_FIELD_WARNING);
			case ERROR:
				return Optional.of(ISharedImages.IMG_DEC_FIELD_ERROR);
			default:
				return Optional.empty();
			}
		}).map(key -> PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(key));
	}

	private Optional<Severity> getProblemSeverity(BookmarkId bookmarkId) {
		return bookmarkProblems.getBookmarkProblems(bookmarkId).stream()
				.map(problem -> bookmarkProblems.getBookmarkProblemDescriptor(problem.getProblemType()).getSeverity())
				.findFirst();
	}

	private Optional<IRemoteBookmarksStore> getRemoteBookmarkStore(BookmarkId bookmarkFolderId) {
		return remoteBookmarksStoreManager.getRemoteBookmarkFolder(bookmarkFolderId)
				.flatMap(f -> remoteBookmarksStoreManager.getRemoteBookmarksStore(f.getRemoteBookmarkStoreId()));
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