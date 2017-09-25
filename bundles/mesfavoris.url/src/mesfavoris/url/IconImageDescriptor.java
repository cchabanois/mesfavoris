package mesfavoris.url;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Optional;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

public class IconImageDescriptor extends ImageDescriptor {
	private final byte[] favIconBytes;
	private ImageData imageData;

	public IconImageDescriptor(byte[] favIconBytes) {
		this.favIconBytes = favIconBytes;
	}

	@Override
	public ImageData getImageData() {
		if (imageData == null) {
			imageData = loadImageData();
		}
		return imageData;
	}

	private ImageData loadImageData() {
		ImageData[] imageDatas;
		try {
			imageDatas = new ImageLoader().load(new ByteArrayInputStream(favIconBytes));
		} catch (SWTException e) {
			return null;
		}
		Optional<ImageData> optionalImageData = Arrays.stream(imageDatas).sorted((imageData1,
				imageData2) -> distanceFrom16x16ImageData(imageData1) - distanceFrom16x16ImageData(imageData2))
				.findFirst();
		if (!optionalImageData.isPresent()) {
			return null;
		}
		ImageData imageData = optionalImageData.get();
		if (imageData.width <= 16 && imageData.height <= 16) {
			return imageData;
		}
		return imageData.scaledTo(16, 16);
	}

	private int distanceFrom16x16ImageData(ImageData imageData) {
		return imageData.width * imageData.height - 16 * 16;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(favIconBytes);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof IconImageDescriptor)) {
			return false;
		}
		IconImageDescriptor descriptor = (IconImageDescriptor) obj;
		return Arrays.equals(descriptor.favIconBytes, favIconBytes);
	}

}
