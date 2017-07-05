package mesfavoris.bookmarktype;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;

import mesfavoris.model.Bookmark;

public abstract class AbstractBookmarkLabelProvider implements IBookmarkLabelProvider {
	
	@Override
	public StyledString getStyledText(Context context, Bookmark bookmark) {
		String name = bookmark.getPropertyValue(Bookmark.PROPERTY_NAME);
		if (name == null) {
			name = "unnamed";
		}
		return new StyledString(name);
	}
	
	@Override
	public ImageDescriptor getImageDescriptor(Context context, Bookmark bookmark) {
		return null;
	}
	
}
