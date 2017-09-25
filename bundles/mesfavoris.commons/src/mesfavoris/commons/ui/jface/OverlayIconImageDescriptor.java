package mesfavoris.commons.ui.jface;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

/**
 * An OverlayIcon consists of a main icon and several adornments.
 */
public class OverlayIconImageDescriptor extends CompositeImageDescriptor {

	static final int DEFAULT_WIDTH = 22;

	static final int DEFAULT_HEIGHT = 16;

	private Point fSize = null;

	private ImageDescriptor fBase;

	private ImageDescriptor fOverlays[][];

	public OverlayIconImageDescriptor(ImageDescriptor base, ImageDescriptor[] overlays) {
		this(base, new ImageDescriptor[][] { { overlays[0] }, { overlays[1] }, { overlays[2] } });
	}

	public OverlayIconImageDescriptor(ImageDescriptor base, ImageDescriptor[][] overlays) {
		fBase = base;
		fOverlays = overlays;
	}

	protected void drawBottomLeft(ImageDescriptor[] overlays) {
		if (overlays == null) {
			return;
		}
		int length = overlays.length;
		int x = 0;
		for (int i = 0; i < 3; i++) {
			if (i < length && overlays[i] != null) {
				ImageData id = overlays[i].getImageData();
				drawImage(id, x, getSize().y - id.height);
				x += id.width;
			}
		}
	}

	protected void drawBottomRight(ImageDescriptor[] overlays) {
		if (overlays == null) {
			return;
		}
		int length = overlays.length;
		int x = getSize().x;
		for (int i = 2; i >= 0; i--) {
			if (i < length && overlays[i] != null) {
				ImageData id = overlays[i].getImageData();
				x -= id.width;
				drawImage(id, x, getSize().y - id.height);
			}
		}
	}

	/**
	 * @see CompositeImageDescriptor#drawCompositeImage(int, int)
	 */
	@Override
	protected void drawCompositeImage(int width, int height) {
		ImageData bg;
		if (fBase == null || (bg = fBase.getImageData()) == null) {
			bg = DEFAULT_IMAGE_DATA;
		}
		drawImage(bg, 0, 0);

		if (fOverlays != null) {
			if (fOverlays.length > 0) {
				drawTopRight(fOverlays[0]);
			}

			if (fOverlays.length > 1) {
				drawBottomRight(fOverlays[1]);
			}

			if (fOverlays.length > 2) {
				drawBottomLeft(fOverlays[2]);
			}

			if (fOverlays.length > 3) {
				drawTopLeft(fOverlays[3]);
			}
		}
	}

	protected void drawTopLeft(ImageDescriptor[] overlays) {
		if (overlays == null) {
			return;
		}
		int length = overlays.length;
		int x = 0;
		for (int i = 0; i < 3; i++) {
			if (i < length && overlays[i] != null) {
				ImageData id = overlays[i].getImageData();
				drawImage(id, x, 0);
				x += id.width;
			}
		}
	}

	protected void drawTopRight(ImageDescriptor[] overlays) {
		if (overlays == null) {
			return;
		}
		int length = overlays.length;
		int x = getSize().x;
		for (int i = 2; i >= 0; i--) {
			if (i < length && overlays[i] != null) {
				ImageData id = overlays[i].getImageData();
				x -= id.width;
				drawImage(id, x, 0);
			}
		}
	}

	/**
	 * @see CompositeImageDescriptor#getSize()
	 */
	@Override
	protected Point getSize() {
		if (fSize == null) {
			ImageData data = fBase.getImageData();
			fSize = new Point(data.width, data.height);
		}
		return fSize;
	}
}
