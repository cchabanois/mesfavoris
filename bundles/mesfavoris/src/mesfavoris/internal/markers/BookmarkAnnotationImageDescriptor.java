package mesfavoris.internal.markers;

import java.util.Optional;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

import mesfavoris.BookmarksPlugin;
import mesfavoris.internal.numberedbookmarks.BookmarkNumber;
import mesfavoris.internal.numberedbookmarks.NumberedBookmarkDecorationProvider;

/**
 * Image descriptor for bookmark annotation image
 * 
 * @author cchabanois
 *
 */
public class BookmarkAnnotationImageDescriptor extends CompositeImageDescriptor {
	private final Optional<BookmarkNumber> bookmarkNumber;
	private final ImageDescriptor base;
	private final Optional<ImageDescriptor> bookmarkNumberImageDescriptor;

	public BookmarkAnnotationImageDescriptor(Optional<BookmarkNumber> bookmarkNumber) {
		this.bookmarkNumber = bookmarkNumber;
		this.base = BookmarksPlugin.getImageDescriptor("icons/bookmark-16.png");
		this.bookmarkNumberImageDescriptor = bookmarkNumber
				.map(number -> NumberedBookmarkDecorationProvider.getImageDescriptor(number));
	}

	/**
	 * Draw the overlays for the receiver.
	 *
	 * @param overlaysArray
	 */
	private void drawBookmarkNumberOverlay() {
		if (!bookmarkNumberImageDescriptor.isPresent()) {
			return;
		}
		ImageData overlayData = bookmarkNumberImageDescriptor.get().getImageData();
		drawImage(overlayData, 5, 3);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bookmarkNumber == null) ? 0 : bookmarkNumber.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BookmarkAnnotationImageDescriptor other = (BookmarkAnnotationImageDescriptor) obj;
		if (bookmarkNumber == null) {
			if (other.bookmarkNumber != null)
				return false;
		} else if (!bookmarkNumber.equals(other.bookmarkNumber))
			return false;
		return true;
	}

	@Override
	protected void drawCompositeImage(int width, int height) {
		drawImage(base.getImageData(), 0, 0);
		drawBookmarkNumberOverlay();
	}

	@Override
	protected Point getSize() {
		return new Point(16, 16);
	}

	@Override
	protected int getTransparentPixel() {
		return base.getImageData().transparentPixel;
	}
}
