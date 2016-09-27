package mesfavoris.viewers;

import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Predicate;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import mesfavoris.BookmarksPlugin;
import mesfavoris.bookmarktype.IBookmarkLabelProvider;
import mesfavoris.commons.core.AdapterUtils;
import mesfavoris.internal.views.virtual.BookmarkLink;
import mesfavoris.internal.views.virtual.VirtualBookmarkFolder;
import mesfavoris.model.Bookmark;

public class BookmarksLabelProvider extends LabelProvider implements ILabelProvider, IStyledLabelProvider {
	private static final String ICON_VIRTUAL_BOOKMARK_FOLDER = "icons/ovr16/virt_ovr.png";
	private static final String ICON_BOOKMARK_LINK = "icons/ovr16/link_ovr.png";
	private final IBookmarkLabelProvider bookmarkLabelProvider;
	private final ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());
	private final Color commentColor;
	private final StylerProvider stylerProvider = new StylerProvider();
	private final Font boldFont;
	private final Color disabledColor;
	private final Predicate<Bookmark> selectedBookmarkPredicate;
	private final Predicate<Bookmark> disabledBookmarkPredicate;
	private final Predicate<Bookmark> dirtyBookmarkPredicate;
	private final IBookmarkDecorationProvider bookmarkDecorationProvider;
	private final IBookmarkCommentProvider bookmarkCommentProvider;

	public BookmarksLabelProvider(Predicate<Bookmark> selectedBookmarkPredicate,
			Predicate<Bookmark> disabledBookmarkPredicate, Predicate<Bookmark> dirtyBookmarkPredicate, IBookmarkDecorationProvider bookmarkDecorationProvider,
			IBookmarkLabelProvider bookmarkLabelProvider, IBookmarkCommentProvider bookmarkCommentProvider) {
		this.selectedBookmarkPredicate = selectedBookmarkPredicate;
		this.bookmarkLabelProvider = bookmarkLabelProvider;
		this.disabledBookmarkPredicate = disabledBookmarkPredicate;
		this.dirtyBookmarkPredicate = dirtyBookmarkPredicate;
		this.bookmarkDecorationProvider = bookmarkDecorationProvider;
		this.bookmarkCommentProvider = bookmarkCommentProvider;
		this.boldFont = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
		this.disabledColor = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_GRAY);

		this.commentColor = new Color(PlatformUI.getWorkbench().getDisplay(), 63, 127, 95);
	}

	public StyledString getStyledText(final Object element) {
		Bookmark bookmark = (Bookmark) AdapterUtils.getAdapter(element, Bookmark.class);
		String comment = bookmarkCommentProvider.apply(bookmark);
		boolean hasComment = comment != null && comment.trim().length() > 0;
		boolean isDisabled = disabledBookmarkPredicate.test(bookmark);
		boolean isSelectedBookmark = selectedBookmarkPredicate.test(bookmark);
		StyledString styledString = new StyledString();
		if (dirtyBookmarkPredicate.test(bookmark)) {
			styledString.append("> ");
		}
		styledString.append(bookmarkLabelProvider.getStyledText(bookmark));
		if (isDisabled || isSelectedBookmark) {
			Color color = null;
			Font font = null;
			if (isDisabled) {
				color = disabledColor;
			}
			if (isSelectedBookmark) {
				font = boldFont;
			}
			styledString.setStyle(0, styledString.length(), stylerProvider.getStyler(font, color, null));
		}

		if (hasComment) {
			Color color = commentColor;
			Font font = null;
			if (isDisabled) {
				color = disabledColor;
			}
			if (isSelectedBookmark) {
				font = boldFont;
			}
			styledString.append(" - " + comment, stylerProvider.getStyler(font, color, null));
		}
		return styledString;
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
		ImageDescriptor[] overlayImages = bookmarkDecorationProvider.apply(bookmark);
		if (element instanceof BookmarkLink) {
			ImageDescriptor imageDescriptor = BookmarksPlugin.getImageDescriptor(ICON_BOOKMARK_LINK);
			overlayImages[IDecoration.BOTTOM_RIGHT] = imageDescriptor;
		}
		if (element instanceof VirtualBookmarkFolder) {
			ImageDescriptor imageDescriptor = BookmarksPlugin.getImageDescriptor(ICON_VIRTUAL_BOOKMARK_FOLDER);
			overlayImages[IDecoration.BOTTOM_RIGHT] = imageDescriptor;
		}
		return overlayImages;
	}
	
	public static class DefaultBookmarkCommentProvider implements IBookmarkCommentProvider {

		@Override
		public String apply(Bookmark bookmark) {
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

	}

	@Override
	public String getText(Object element) {
		return getStyledText(element).toString();
	}

}