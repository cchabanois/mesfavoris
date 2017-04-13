package mesfavoris.tests.commons.swt;

import static org.junit.Assert.fail;

import java.text.MessageFormat;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;

/**
 * test helper for SWT image
 *
 */
public class ImageAssert {

	public static void assertImageDataIs(ImageData expectedImageData,
			ImageData actualImageData) {
		if (expectedImageData.width != actualImageData.width
				|| expectedImageData.height != actualImageData.height) {
			fail(MessageFormat
					.format(
							"Image data do not have the same dimensions ({0}x{1} expected, got {2}x{3})",
							expectedImageData.width, expectedImageData.height,
							actualImageData.width, actualImageData.height));
		}

		for (int y = 0; y < expectedImageData.height; y++) {
			for (int x = 0; x < expectedImageData.width; x++) {
				int actualPixel = actualImageData.getPixel(x, y);
				int expectedPixel = expectedImageData.getPixel(x, y);
				RGB actualRGB = actualImageData.palette.getRGB(actualPixel);
				RGB expectedRGB = expectedImageData.palette
						.getRGB(expectedPixel);
				if (!actualRGB.equals(expectedRGB)) {
					fail(MessageFormat.format(
							"Image data do not match at ({0},{1})", x, y));
				}
			}
		}
	}
	
	
}
