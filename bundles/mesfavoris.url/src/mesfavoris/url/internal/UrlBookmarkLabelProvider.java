package mesfavoris.url.internal;

import static mesfavoris.url.UrlBookmarkProperties.PROP_FAVICON;
import static mesfavoris.url.UrlBookmarkProperties.PROP_ICON;
import static mesfavoris.url.UrlBookmarkProperties.PROP_URL;

import java.util.Base64;

import org.eclipse.jface.resource.ImageDescriptor;

import mesfavoris.bookmarktype.AbstractBookmarkLabelProvider;
import mesfavoris.model.Bookmark;
import mesfavoris.url.IconImageDescriptor;

public class UrlBookmarkLabelProvider extends AbstractBookmarkLabelProvider {

	@SuppressWarnings("deprecation")
	@Override
	public ImageDescriptor getImageDescriptor(Context context, Bookmark bookmark) {
		String iconAsBase64 = bookmark.getPropertyValue(PROP_ICON);
		if (iconAsBase64 == null) {
			iconAsBase64 = bookmark.getPropertyValue(PROP_FAVICON);
		}
		if (iconAsBase64 == null) {
			if (bookmark.getPropertyValue(PROP_URL) != null) {
				// taken from http://www.famfamfam.com/lab/icons/mini/icons/page_tag_blue.gif
				return Activator.getImageDescriptor("icons/obj16/page_tag_blue.gif");
			} else {
				return null;
			}
		}
		byte[] favIconBytes = Base64.getDecoder().decode(iconAsBase64);
		return new IconImageDescriptor(favIconBytes);
	}
	
	@Override
	public boolean canHandle(Context context, Bookmark bookmark) {
		return bookmark.getPropertyValue(PROP_URL) != null;
	}

}
