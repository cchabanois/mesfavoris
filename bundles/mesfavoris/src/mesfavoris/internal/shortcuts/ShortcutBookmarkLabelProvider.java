package mesfavoris.internal.shortcuts;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;

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
		ImageDescriptor imageDescriptor = null;
		if (bookmark != null) {
			imageDescriptor = bookmarkLabelProvider.getImageDescriptor(context, bookmark);
		} else {
			imageDescriptor = super.getImageDescriptor(context, bookmark);
		}
		ImageDescriptor overlayImageDescriptor = BookmarksPlugin.getImageDescriptor(IUIConstants.IMG_BOOKMARK_LINK);
		ImageDescriptor[] overlayImages = new ImageDescriptor[5];
		overlayImages[IDecoration.BOTTOM_LEFT] = overlayImageDescriptor;
		return new OverlayIconImageDescriptor(imageDescriptor, overlayImages, 16, 16);
	}

	@Override
	public boolean canHandle(Context context, Bookmark bookmark) {
		return getBookmarksTree(context) != null
				&& bookmark.getPropertyValue(ShortcutBookmarkProperties.PROP_BOOKMARK_ID) != null;
	}

	private BookmarksTree getBookmarksTree(Context context) {
		return context.get(Context.BOOKMARKS_TREE);
	}

}
