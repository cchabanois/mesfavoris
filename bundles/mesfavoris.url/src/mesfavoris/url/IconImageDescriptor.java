package mesfavoris.url;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Optional;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

public class IconImageDescriptor extends ImageDescriptor {
	private final byte[] iconBytes;
	private ImageData[] iconImageData;
	private ImageData imageData100;
	private ImageData imageData150;
	private ImageData imageData200;

	public IconImageDescriptor(byte[] iconBytes) {
		this.iconBytes = iconBytes;
	}

	@Override
	public ImageData getImageData(int zoom) {
		if (iconImageData == null) {
			iconImageData = loadIconImageData(iconBytes);
		}
		switch (zoom) {
		case 100:
			return getImageData100(iconImageData);
		case 150:
			return getImageData150(iconImageData);
		case 200:
			return getImageData200(iconImageData);
		default:
			return getImageData100(iconImageData);
		}
	}

	private ImageData getImageData100(ImageData[] iconImageData) {
		if (imageData100 == null) {
			imageData100 = getImageData(iconImageData, 16);
		}
		return imageData100;
	}

	private ImageData getImageData150(ImageData[] iconImageData) {
		if (imageData150 == null) {
			imageData150 = getImageData(iconImageData, 24);
		}
		return imageData150;
	}

	private ImageData getImageData200(ImageData[] iconImageData) {
		if (imageData200 == null) {
			imageData200 = getImageData(iconImageData, 32);
		}
		return imageData200;
	}

	private ImageData getImageData(ImageData[] iconImageData, int size) {
		Optional<ImageData> optionalImageData = Arrays.stream(iconImageData).sorted((imageData1,
				imageData2) -> distanceFromTargetSize(imageData1, size) - distanceFromTargetSize(imageData2, size))
				.findFirst();
		if (!optionalImageData.isPresent()) {
			return null;
		}
		ImageData imageData = optionalImageData.get();
		if (imageData.width == size && imageData.height == size) {
			return imageData;
		}
		return imageData.scaledTo(size, size);
	}

	private ImageData[] loadIconImageData(byte[] iconBytes) {
		try {
			return new ImageLoader().load(new ByteArrayInputStream(iconBytes));
		} catch (SWTException e) {
			return new ImageData[0];
		}
	}

	private int distanceFromTargetSize(ImageData imageData, int targetSize) {
		return imageData.width * imageData.height - targetSize * targetSize;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(iconBytes);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof IconImageDescriptor)) {
			return false;
		}
		IconImageDescriptor descriptor = (IconImageDescriptor) obj;
		return Arrays.equals(descriptor.iconBytes, iconBytes);
	}

}
