package mesfavoris.internal.views.properties;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class PropertyLabelProvider extends LabelProvider implements IColorProvider {
	private final boolean isGrayed;
	private final PropertyIcon propertyIcon;

	public static enum PropertyIcon {
		NONE, INFO, WARNING
	}	
	
	public PropertyLabelProvider(boolean isGrayed, PropertyIcon propertyIcon) {
		this.isGrayed = isGrayed;
		this.propertyIcon = propertyIcon;
	}

	@Override
	public Image getImage(Object element) {
		if (propertyIcon == PropertyIcon.NONE) {
			return null;
		}
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		if (propertyIcon == PropertyIcon.INFO) {
			return sharedImages.getImage(ISharedImages.IMG_OBJS_INFO_TSK);
		} else {
			return sharedImages.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
		}
	}

	@Override
	public Color getForeground(Object element) {
		if (isGrayed) {
			return PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_GRAY);
		}
		return null;
	}

	@Override
	public Color getBackground(Object element) {
		return null;
	}

}