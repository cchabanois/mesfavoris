package mesfavoris.url;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.graphics.ImageData;
import org.junit.Test;

import com.google.common.io.ByteStreams;

public class IconImageDescriptorTest {

	@Test
	public void testHashCode() throws IOException {
		// Given
		IconImageDescriptor imageDescriptor1 = new IconImageDescriptor(getImageAsBytes("lemonde-favicon.ico"));
		IconImageDescriptor imageDescriptor2 = new IconImageDescriptor(getImageAsBytes("lemonde-favicon.ico"));
	
		// When/Then
		assertEquals(imageDescriptor1.hashCode(),imageDescriptor2.hashCode());
	}	
	
	@Test
	public void testEquals() throws IOException {
		// Given
		IconImageDescriptor imageDescriptor1 = new IconImageDescriptor(getImageAsBytes("lemonde-favicon.ico"));
		IconImageDescriptor imageDescriptor2 = new IconImageDescriptor(getImageAsBytes("lemonde-favicon.ico"));
	
		// When/Then
		assertEquals(imageDescriptor1,imageDescriptor2);
	}
	
	@Test
	public void testGetImageData() throws IOException {
		// Given
		IconImageDescriptor imageDescriptor = new IconImageDescriptor(getImageAsBytes("lemonde-favicon.ico"));

		// When
		ImageData imageData1 = imageDescriptor.getImageData();
		ImageData imageData2 = imageDescriptor.getImageData();
		
		// Then
		assertSame(imageData1, imageData2);
		
	}
	
	private byte[] getImageAsBytes(String resourceName) throws IOException {
		try (InputStream is = getClass().getResourceAsStream(resourceName)) {
			return ByteStreams.toByteArray(is);
		}
	}
	
}
