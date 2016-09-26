package mesfavoris.internal.numberedbookmarks;

import java.util.Optional;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;

import mesfavoris.BookmarksPlugin;
import mesfavoris.model.Bookmark;
import mesfavoris.viewers.IBookmarkDecorationProvider;

public class NumberedBookmarkDecorationProvider implements IBookmarkDecorationProvider {
	private final NumberedBookmarks numberedBookmarks;

	public NumberedBookmarkDecorationProvider(NumberedBookmarks numberedBookmarks) {
		this.numberedBookmarks = numberedBookmarks;
	}

	@Override
	public ImageDescriptor[] apply(Bookmark bookmark) {
		ImageDescriptor[] overlayImages = new ImageDescriptor[5];
		Optional<BookmarkNumber> bookmarkNumber = numberedBookmarks.getBookmarkNumber(bookmark.getId());

		if (bookmarkNumber.isPresent()) {
			overlayImages[IDecoration.TOP_LEFT] = getImageDescriptor(bookmarkNumber.get());
		}
		return overlayImages;
	}

	public static ImageDescriptor getImageDescriptor(BookmarkNumber bookmarkNumber) {
		switch (bookmarkNumber) {
		case ONE:
			return BookmarksPlugin.getImageDescriptor("icons/ovr16/1_ovr.png");
		case TWO:
			return BookmarksPlugin.getImageDescriptor("icons/ovr16/2_ovr.png");
		case THREE:
			return BookmarksPlugin.getImageDescriptor("icons/ovr16/3_ovr.png");
		case FOUR:
			return BookmarksPlugin.getImageDescriptor("icons/ovr16/4_ovr.png");
		case FIVE:
			return BookmarksPlugin.getImageDescriptor("icons/ovr16/5_ovr.png");
		case SIX:
			return BookmarksPlugin.getImageDescriptor("icons/ovr16/6_ovr.png");
		case SEVEN:
			return BookmarksPlugin.getImageDescriptor("icons/ovr16/7_ovr.png");
		case EIGHT:
			return BookmarksPlugin.getImageDescriptor("icons/ovr16/8_ovr.png");
		case NINE:
			return BookmarksPlugin.getImageDescriptor("icons/ovr16/9_ovr.png");
		case ZERO:
			return BookmarksPlugin.getImageDescriptor("icons/ovr16/10_ovr.png");
		}
		return null;
	}

}
