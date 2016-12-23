package mesfavoris.internal.markers;

import java.util.Optional;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.IAnnotationImageProvider;
import org.eclipse.ui.texteditor.MarkerAnnotation;

import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.numberedbookmarks.BookmarkNumber;
import mesfavoris.internal.numberedbookmarks.NumberedBookmarks;
import mesfavoris.model.BookmarkId;

/**
 * Provides image for bookmark annotation
 * 
 * @author cchabanois
 *
 */
public class BookmarkAnnotationImageProvider implements IAnnotationImageProvider {
	private final ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());
	private final NumberedBookmarks numberedBookmarks;

	public BookmarkAnnotationImageProvider() {
		this.numberedBookmarks = BookmarksPlugin.getDefault().getNumberedBookmarks();
	}

	@Override
	public Image getManagedImage(Annotation annotation) {
		if (!(annotation instanceof MarkerAnnotation)) {
			return null;
		}
		try {
			MarkerAnnotation markerAnnotation = (MarkerAnnotation) annotation;
			IMarker marker = markerAnnotation.getMarker();
			if (!BookmarksMarkers.MARKER_TYPE.equals(marker.getType())) {
				return null;
			}
			String attributeBookmarkId = (String) marker.getAttribute(BookmarksMarkers.BOOKMARK_ID);
			Optional<BookmarkNumber> bookmarkNumber = numberedBookmarks
					.getBookmarkNumber(new BookmarkId(attributeBookmarkId));
			return getBookmarkAnnotationImage(bookmarkNumber);
		} catch (CoreException e) {
			return null;
		}
	}

	private Image getBookmarkAnnotationImage(Optional<BookmarkNumber> bookmarkNumber) {
		BookmarkAnnotationImageDescriptor imageDescriptor = new BookmarkAnnotationImageDescriptor(bookmarkNumber);
		return (Image) this.resourceManager.get(imageDescriptor);
	}

	@Override
	public String getImageDescriptorId(Annotation annotation) {
		return null;
	}

	@Override
	public ImageDescriptor getImageDescriptor(String imageDescritporId) {
		return null;
	}

}
