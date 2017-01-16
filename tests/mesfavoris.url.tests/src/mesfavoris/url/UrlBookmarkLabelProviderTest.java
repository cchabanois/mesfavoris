package mesfavoris.url;

import static org.junit.Assert.assertEquals;

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

	private String getImageAsBase64(String resourceName) throws IOException {
		try (InputStream is = getClass().getResourceAsStream(resourceName)) {
			return Base64.getEncoder().encodeToString(ByteStreams.toByteArray(is));
		}
	}

}
