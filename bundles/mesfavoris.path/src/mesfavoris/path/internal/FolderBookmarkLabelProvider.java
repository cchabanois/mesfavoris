package mesfavoris.path.internal;

import static mesfavoris.path.PathBookmarkProperties.PROP_FOLDER_PATH;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import mesfavoris.bookmarktype.AbstractBookmarkLabelProvider;
import mesfavoris.model.Bookmark;

public class FolderBookmarkLabelProvider extends AbstractBookmarkLabelProvider {
	
	public FolderBookmarkLabelProvider() {
	}

	@Override
	public Image getImage(Object element) {
		ImageDescriptor imageDescriptor = Activator.getImageDescriptor("icons/obj16/folder.png");
		return resourceManager.createImage(imageDescriptor);
	}
	
	@Override
	public boolean handlesBookmark(Bookmark bookmark) {
		return bookmark.getPropertyValue(PROP_FOLDER_PATH) != null;
	}
}
