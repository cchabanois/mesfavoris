package mesfavoris.internal.shortcuts;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.StyledString;

import mesfavoris.bookmarktype.AbstractBookmarkLabelProvider;
import mesfavoris.bookmarktype.IBookmarkLabelProvider;
import mesfavoris.commons.ui.jface.OverlayIconImageDescriptor;
import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.IUIConstants;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;

public class ShortcutBookmarkLabelProvider extends AbstractBookmarkLabelProvider {
	private final IBookmarkLabelProvider bookmarkLabelProvider;

	public ShortcutBookmarkLabelProvider() {
		this.bookmarkLabelProvider = BookmarksPlugin.getDefault().getBookmarkLabelProvider();
	}

	@Override
	public ImageDescriptor getImageDescriptor(Context context, Bookmark shortcutBookmark) {
		BookmarkId bookmarkId = new BookmarkId(
				shortcutBookmark.getPropertyValue(ShortcutBookmarkProperties.PROP_BOOKMARK_ID));
		if (bookmarkId.equals(shortcutBookmark.getId())) {
			return null;
		}
		Bookmark bookmark = getBookmarksTree(context).getBookmark(bookmarkId);
		ImageDescriptor image = bookmarkLabelProvider.getImageDescriptor(context, bookmark);
		ImageDescriptor overlayImageDescriptor = BookmarksPlugin.getImageDescriptor(IUIConstants.IMG_BOOKMARK_LINK);
		ImageDescriptor[] overlayImages = new ImageDescriptor[5];
		overlayImages[IDecoration.BOTTOM_LEFT] = overlayImageDescriptor;
		return new OverlayIconImageDescriptor(image, overlayImages, 16, 16);
	}

//	@Override
//	public StyledString getStyledText(Context context, Bookmark shortcutBookmark) {
//		BookmarkId bookmarkId = new BookmarkId(
//				shortcutBookmark.getPropertyValue(ShortcutBookmarkProperties.PROP_BOOKMARK_ID));
//		if (bookmarkId.equals(shortcutBookmark.getId())) {
//			return new StyledString("");
//		}
//		Bookmark bookmark = getBookmarksTree(context).getBookmark(bookmarkId);
//		return bookmarkLabelProvider.getStyledText(context, bookmark);
//	}

	@Override
	public boolean canHandle(Context context, Bookmark bookmark) {
		return getBookmarksTree(context) != null
				&& bookmark.getPropertyValue(ShortcutBookmarkProperties.PROP_BOOKMARK_ID) != null;
	}

	private BookmarksTree getBookmarksTree(Context context) {
		return context.get(Context.BOOKMARKS_TREE);
	}

}
