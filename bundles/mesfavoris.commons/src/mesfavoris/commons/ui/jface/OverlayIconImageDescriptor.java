package mesfavoris.commons.ui.jface;

import java.util.Arrays;
import java.util.function.Supplier;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageDataProvider;
import org.eclipse.swt.graphics.Point;

/**
 * An OverlayIcon consists of a main icon and several adornments.
 * 
 * Code is mostly taken from {@link DecorationOverlayIcon}
 */
public class OverlayIconImageDescriptor extends CompositeImageDescriptor {

	private final ImageDescriptor baseImageDescriptor;

    // the overlay images
    private final ImageDescriptor[] overlays;

	private final ImageDataProvider baseImageDataProvider;

	/**
	 * The size of the base image (that's also the size of this composite image)
	 */
	private final Supplier<Point> size;

	/**
	 * Create a decoration overlay icon
	 *
	 * @param baseImageDescriptor
	 *            the base image descriptor
	 * @param overlayImageDescriptor
	 *            the overlay image descriptors for each quatrant
	 */
	public OverlayIconImageDescriptor(ImageDescriptor baseImageDescriptor, ImageDescriptor[] overlaysImageDescriptors) {
		this.baseImageDescriptor = baseImageDescriptor;
		this.overlays = overlaysImageDescriptors;
		this.baseImageDataProvider = createCachedImageDataProvider(baseImageDescriptor);
		this.size = () -> {
			int zoomLevel = getZoomLevel();
			if (zoomLevel != 0) {
				ImageData data = baseImageDataProvider.getImageData(zoomLevel);
				if (data != null) {
					return new Point(autoScaleDown(data.width), autoScaleDown(data.height));
				}
			}
			ImageData data = baseImageDataProvider.getImageData(100);
			return new Point(data.width, data.height);
		};
	}

	/**
     * Draw the overlays for the receiver.
     * @param overlaysArray
     */
    private void drawOverlays(ImageDescriptor[] overlaysArray) {

        for (int i = 0; i < overlays.length; i++) {
            ImageDescriptor overlay = overlaysArray[i];
            if (overlay == null) {
				continue;
			}
            CachedImageDataProvider overlayImageProvider = createCachedImageDataProvider(overlay);

            switch (i) {
            case IDecoration.TOP_LEFT:
				drawImage(overlayImageProvider, 0, 0);
                break;
            case IDecoration.TOP_RIGHT:
				int overlayWidth = overlayImageProvider.getWidth();
				drawImage(overlayImageProvider, getSize().x - overlayWidth, 0);
                break;
            case IDecoration.BOTTOM_LEFT:
				int overlayHeight = overlayImageProvider.getWidth();
				drawImage(overlayImageProvider, 0, getSize().y - overlayHeight);
                break;
            case IDecoration.BOTTOM_RIGHT:
				overlayWidth = overlayImageProvider.getWidth();
				overlayHeight = overlayImageProvider.getHeight();
				drawImage(overlayImageProvider, getSize().x - overlayWidth, getSize().y - overlayHeight);
                break;
            }
        }
    }

	@Override
	public boolean equals(Object o) {
        if (!(o instanceof OverlayIconImageDescriptor)) {
			return false;
		}
        OverlayIconImageDescriptor other = (OverlayIconImageDescriptor) o;
		return baseImageDescriptor.equals(other.baseImageDescriptor)
                && Arrays.equals(overlays, other.overlays);
    }

    @Override
	public int hashCode() {
		int code = System.identityHashCode(baseImageDescriptor);
        for (ImageDescriptor overlay : overlays) {
            if (overlay != null) {
				code ^= overlay.hashCode();
			}
        }
        return code;
    }

    @Override
	protected void drawCompositeImage(int width, int height) {
    	if (overlays.length > IDecoration.UNDERLAY) {
	        ImageDescriptor underlay = overlays[IDecoration.UNDERLAY];
	        if (underlay != null) {
				drawImage(createCachedImageDataProvider(underlay), 0, 0);
			}
    	}
    	if (overlays.length > IDecoration.REPLACE && overlays[IDecoration.REPLACE] != null) {
    		drawImage(createCachedImageDataProvider(overlays[IDecoration.REPLACE]), 0, 0);
    	} else {
			drawImage(baseImageDataProvider, 0, 0);
    	}
        drawOverlays(overlays);
    }

	@Override
	protected Point getSize() {
		return size.get();
    }

    @Override
	protected int getTransparentPixel() {
		return baseImageDataProvider.getImageData(100).transparentPixel;
    }

}
