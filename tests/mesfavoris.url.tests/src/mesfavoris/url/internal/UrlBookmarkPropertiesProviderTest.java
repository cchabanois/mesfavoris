package mesfavoris.url.internal;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.junit.Test;

import mesfavoris.model.Bookmark;
import mesfavoris.url.UrlBookmarkProperties;

public class UrlBookmarkPropertiesProviderTest {
	private final UrlBookmarkPropertiesProvider provider = new UrlBookmarkPropertiesProvider();

	@Test
	public void testUrlBookmarkPropertiesProvider() throws Exception {
		assertTitleAndFavIcon("GitHub - cchabanois/mesfavoris: Bookmarks eclipse plugin", "fluidicon.png",
				"https://github.com/cchabanois/mesfavoris");
	}

	@Test
	public void testUrlIsUsedAsTitleWhenAuthenticationIsNeeded() throws IOException {
		String url = "https://docs.google.com/a/salesforce.com/file/d/0B97G1IRAgxIEanhJTmkyS0NFem8/edit";
		assertTitleAndFavIcon(url, "infinite_arrow_favicon_4.ico", url);
	}

	private void assertTitleAndFavIcon(String expectedTitle, String expectedIcon, String url) throws IOException {
		Map<String, String> bookmarkProperties = new HashMap<>();
		provider.addBookmarkProperties(bookmarkProperties, null, new StructuredSelection(new URL(url)),
				new NullProgressMonitor());
		assertEquals(expectedTitle, bookmarkProperties.get(Bookmark.PROPERTY_NAME));
		assertEquals(expectedIcon == null ? null : getImageAsIconBase64(expectedIcon),
				bookmarkProperties.get(UrlBookmarkProperties.PROP_ICON));
	}

	private String getImageAsIconBase64(String resourceName) throws IOException {
		try (InputStream is = getClass().getResourceAsStream(resourceName)) {
			ImageData[] imageDatas = new ImageLoader().load(is);
			ImageData imageData = imageDatas[0].scaledTo(16, 16);
			return Base64.getEncoder().encodeToString(asBytes(imageData, SWT.IMAGE_ICO));
		}
	}

	private byte[] asBytes(ImageData imageData, int format) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			ImageLoader loader = new ImageLoader();
			loader.data = new ImageData[] { imageData };
			loader.save(baos, format);
			return baos.toByteArray();
		} catch (IOException e) {
			return new byte[0];
		}
	}
}
