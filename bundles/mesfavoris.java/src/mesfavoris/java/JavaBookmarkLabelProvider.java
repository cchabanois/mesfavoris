package mesfavoris.java;

import static mesfavoris.java.JavaBookmarkProperties.KIND_ANNOTATION;
import static mesfavoris.java.JavaBookmarkProperties.KIND_CLASS;
import static mesfavoris.java.JavaBookmarkProperties.KIND_ENUM;
import static mesfavoris.java.JavaBookmarkProperties.KIND_FIELD;
import static mesfavoris.java.JavaBookmarkProperties.KIND_INTERFACE;
import static mesfavoris.java.JavaBookmarkProperties.KIND_METHOD;
import static mesfavoris.java.JavaBookmarkProperties.KIND_TYPE;
import static mesfavoris.java.JavaBookmarkProperties.PROP_JAVA_ELEMENT_KIND;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.viewsupport.ImageDescriptorRegistry;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jface.resource.ImageDescriptor;

import mesfavoris.bookmarktype.AbstractBookmarkLabelProvider;
import mesfavoris.model.Bookmark;

public class JavaBookmarkLabelProvider extends AbstractBookmarkLabelProvider {

	@Override
	public ImageDescriptor getImageDescriptor(Context context, Bookmark bookmark) {
		ImageDescriptorRegistry registry = JavaPlugin
				.getImageDescriptorRegistry();
		ImageDescriptor descriptor = null;
		String kind = bookmark.getPropertyValue(PROP_JAVA_ELEMENT_KIND);
		if (KIND_METHOD.equals(kind)) {
			descriptor = JavaElementImageProvider
					.getMethodImageDescriptor(false, 0);
		}
		if (KIND_FIELD.equals(kind)) {
			descriptor = JavaElementImageProvider
					.getFieldImageDescriptor(false, 0);
		}
		if (KIND_ANNOTATION.equals(kind)) {
			descriptor = JavaElementImageProvider
					.getTypeImageDescriptor(false, true, Flags.AccAnnotation, false);			
		}
		if (KIND_ENUM.equals(kind)) {
			descriptor = JavaElementImageProvider
					.getTypeImageDescriptor(false, false, Flags.AccEnum, false);			
		}
		if (KIND_INTERFACE.equals(kind)) {
			descriptor = JavaElementImageProvider
					.getTypeImageDescriptor(false, true, Flags.AccInterface, false);			
		}
		if (KIND_CLASS.equals(kind)) {
			descriptor = JavaElementImageProvider
					.getTypeImageDescriptor(false, false, 0, false);
		}		
		if (KIND_TYPE.equals(kind)) {
			descriptor = JavaElementImageProvider
					.getTypeImageDescriptor(false, false, 0, false);
		}
		return descriptor;
	}

	@Override
	public boolean canHandle(Context context, Bookmark bookmark) {
		return bookmark.getPropertyValue(PROP_JAVA_ELEMENT_KIND) != null;
	}

}
