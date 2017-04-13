package mesfavoris.internal.markers;

import static org.junit.Assert.assertNotNull;

import java.util.Optional;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.junit.Test;

import mesfavoris.internal.numberedbookmarks.BookmarkNumber;
import mesfavoris.tests.commons.swt.ImageAssert;

public class BookmarkAnnotationImageDescriptorTest {

	@Test
	public void testBookmarkAnnotationWithNumber() {
		// Given
		BookmarkAnnotationImageDescriptor imageDescriptor = new BookmarkAnnotationImageDescriptor(
				Optional.of(BookmarkNumber.FIVE));

		// When
		ImageData imageData = imageDescriptor.getImageData();

		// Then
		assertNotNull(imageData);
		ImageData expectedImageData = getImageDataFromResource("bookmarkAnnotation5.png");
		ImageAssert.assertImageDataIs(expectedImageData, imageData);
	}

	@Test
	public void testBookmarkAnnotationWithoutNumber() {
		// Given
		BookmarkAnnotationImageDescriptor imageDescriptor = new BookmarkAnnotationImageDescriptor(
				Optional.empty());

		// When
		ImageData imageData = imageDescriptor.getImageData();

		// Then
		assertNotNull(imageData);
		ImageData expectedImageData = getImageDataFromResource("bookmarkAnnotation.png");
		ImageAssert.assertImageDataIs(expectedImageData, imageData);
	}

	private ImageData getImageDataFromResource(String resourceName) {
		ImageLoader imageLoader = new ImageLoader();
		ImageData[] imageDatas = imageLoader.load(this.getClass().getResourceAsStream(resourceName));
		return imageDatas[0];
	}
	
}
