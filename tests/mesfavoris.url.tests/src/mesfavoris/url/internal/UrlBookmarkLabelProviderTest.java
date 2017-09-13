package mesfavoris.url.internal;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import org.eclipse.jface.resource.ImageDescriptor;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;

import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;
import mesfavoris.url.UrlBookmarkProperties;

public class UrlBookmarkLabelProviderTest {
	private UrlBookmarkLabelProvider urlBookmarkLabelProvider;

	@Before
	public void setUp() {
		urlBookmarkLabelProvider = new UrlBookmarkLabelProvider();
	}
	
	@Test
	public void testGetImageReturns16x16Images() throws IOException {
		// Given
		// lemonde-favicon.ico has 2 images in it : one 32x32 and one 16x16
		Bookmark bookmark = new Bookmark(new BookmarkId(),
				ImmutableMap.of(UrlBookmarkProperties.PROP_ICON, getImageAsBase64("lemonde-favicon.ico")));

		// When
		ImageDescriptor image = urlBookmarkLabelProvider.getImageDescriptor(null, bookmark);
		
		// Then
		assertEquals(16, image.getImageData().width);
		assertEquals(16, image.getImageData().height);
	}

	@Test
	public void testSameImageInstanceIfSameFavIcon() throws IOException {
		// Given
		Bookmark bookmark1 = new Bookmark(new BookmarkId(),
				ImmutableMap.of(UrlBookmarkProperties.PROP_ICON, getImageAsBase64("lemonde-favicon.ico")));
		Bookmark bookmark2 = new Bookmark(new BookmarkId(),
				ImmutableMap.of(UrlBookmarkProperties.PROP_ICON, getImageAsBase64("lemonde-favicon.ico")));
		
		// When
		ImageDescriptor image1 = urlBookmarkLabelProvider.getImageDescriptor(null, bookmark1);
		ImageDescriptor image2 = urlBookmarkLabelProvider.getImageDescriptor(null, bookmark2);
		
		// Then
		assertEquals(image1, image2);
	}
	
	private String getImageAsBase64(String resourceName) throws IOException {
		try (InputStream is = getClass().getResourceAsStream(resourceName)) {
			return Base64.getEncoder().encodeToString(ByteStreams.toByteArray(is));
		}
	}

}
