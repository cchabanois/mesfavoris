package mesfavoris.url;

import static mesfavoris.url.UrlBookmarkProperties.PROP_FAVICON;
import static mesfavoris.url.UrlBookmarkProperties.PROP_URL;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

import mesfavoris.bookmarktype.AbstractBookmarkLabelProvider;
import mesfavoris.model.Bookmark;

public class UrlBookmarkLabelProvider extends AbstractBookmarkLabelProvider {

	private final ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());

	@Override
	public void dispose() {
		resourceManager.dispose();
		super.dispose();
	}

	@Override
	public Image getImage(Object element) {
		Bookmark bookmark = (Bookmark) element;
		String favIconAsBase64 = bookmark.getPropertyValue(PROP_FAVICON);
		if (favIconAsBase64 == null) {
			if (bookmark.getPropertyValue(PROP_URL) != null) {
				// taken from http://www.famfamfam.com/lab/icons/mini/icons/page_tag_blue.gif
				ImageDescriptor imageDescriptor = Activator.getImageDescriptor("icons/obj16/page_tag_blue.gif");
				return resourceManager.createImage(imageDescriptor);
			} else {
				return null;
			}
		}
		byte[] favIconBytes = Base64.getDecoder().decode(favIconAsBase64);
		Optional<ImageData> imageData = getImageData(favIconBytes);
		if (!imageData.isPresent()) {
			return null;
		}
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromImageData(imageData.get());
		return resourceManager.createImage(imageDescriptor);
	}

	private Optional<ImageData> getImageData(byte[] favIconBytes) {
		ImageData[] imageDatas;
		try {
			imageDatas = new ImageLoader().load(new ByteArrayInputStream(favIconBytes));
		} catch (SWTException e) {
			return Optional.empty();
		}
		Optional<ImageData> optionalImageData = Arrays.stream(imageDatas).sorted((imageData1,
				imageData2) -> distanceFrom16x16ImageData(imageData1) - distanceFrom16x16ImageData(imageData2))
				.findFirst();
		if (!optionalImageData.isPresent()) {
			return optionalImageData;
		}
		ImageData imageData = optionalImageData.get();
		if (imageData.width <= 16 && imageData.height <= 16) {
			return optionalImageData;
		}
		return Optional.of(imageData.scaledTo(16, 16));
	}

	private int distanceFrom16x16ImageData(ImageData imageData) {
		return imageData.width * imageData.height - 16 * 16;
	}

	@Override
	public boolean handlesBookmark(Bookmark bookmark) {
		return bookmark.getPropertyValue(PROP_URL) != null;
	}

}
