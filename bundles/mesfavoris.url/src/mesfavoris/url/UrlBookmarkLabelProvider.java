package mesfavoris.url;

import static mesfavoris.url.UrlBookmarkProperties.PROP_FAVICON;
import static mesfavoris.url.UrlBookmarkProperties.PROP_URL;

import java.util.Base64;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.graphics.Image;

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
		FavIconImageDescriptor imageDescriptor = new FavIconImageDescriptor(favIconBytes);
		return resourceManager.createImage(imageDescriptor);
	}

	@Override
	public boolean handlesBookmark(Bookmark bookmark) {
		return bookmark.getPropertyValue(PROP_URL) != null;
	}

}
