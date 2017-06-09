package mesfavoris.url.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;

import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;
import mesfavoris.url.UrlBookmarkProperties;
import mesfavoris.url.internal.UrlBookmarkLabelProvider;

public class UrlBookmarkLabelProviderTest {
	private UrlBookmarkLabelProvider urlBookmarkLabelProvider;

	@Before
	public void setUp() {
		urlBookmarkLabelProvider = UIThreadRunnable.syncExec(()->new UrlBookmarkLabelProvider());
	}
	
	@After
	public void tearDown() {
		UIThreadRunnable.syncExec(()->urlBookmarkLabelProvider.dispose());
	}
	
	@Test
	public void testGetImageReturns16x16Images() throws IOException {
		// Given
		// lemonde-favicon.ico has 2 images in it : one 32x32 and one 16x16
		Bookmark bookmark = new Bookmark(new BookmarkId(),
				ImmutableMap.of(UrlBookmarkProperties.PROP_FAVICON, getImageAsBase64("lemonde-favicon.ico")));

		// When
		Image image = UIThreadRunnable.syncExec(()->urlBookmarkLabelProvider.getImage(bookmark));
		
		// Then
		assertEquals(16, image.getBounds().width);
		assertEquals(16, image.getBounds().height);
	}

	@Test
	public void testSameImageInstanceIfSameFavIcon() throws IOException {
		// Given
		Bookmark bookmark1 = new Bookmark(new BookmarkId(),
				ImmutableMap.of(UrlBookmarkProperties.PROP_FAVICON, getImageAsBase64("lemonde-favicon.ico")));
		Bookmark bookmark2 = new Bookmark(new BookmarkId(),
				ImmutableMap.of(UrlBookmarkProperties.PROP_FAVICON, getImageAsBase64("lemonde-favicon.ico")));
		
		// When
		Image image1 = UIThreadRunnable.syncExec(()->urlBookmarkLabelProvider.getImage(bookmark1));
		Image image2 = UIThreadRunnable.syncExec(()->urlBookmarkLabelProvider.getImage(bookmark2));
		
		// Then
		assertSame(image1, image2);
	}
	
	private String getImageAsBase64(String resourceName) throws IOException {
		try (InputStream is = getClass().getResourceAsStream(resourceName)) {
			return Base64.getEncoder().encodeToString(ByteStreams.toByteArray(is));
		}
	}

}
