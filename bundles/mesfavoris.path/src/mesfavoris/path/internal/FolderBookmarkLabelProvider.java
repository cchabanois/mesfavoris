package mesfavoris.path.internal;

import static mesfavoris.path.PathBookmarkProperties.PROP_FOLDER_PATH;

import org.eclipse.jface.resource.ImageDescriptor;

import mesfavoris.bookmarktype.AbstractBookmarkLabelProvider;
import mesfavoris.model.Bookmark;

public class FolderBookmarkLabelProvider extends AbstractBookmarkLabelProvider {
	
	public FolderBookmarkLabelProvider() {
	}

	@Override
	public ImageDescriptor getImageDescriptor(Context context, Bookmark bookmark) {
		return Activator.getImageDescriptor("icons/obj16/folder.png");
	}
	
	@Override
	public boolean canHandle(Context context, Bookmark bookmark) {
		return bookmark.getPropertyValue(PROP_FOLDER_PATH) != null;
	}
}
