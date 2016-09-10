package mesfavoris.viewers;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import mesfavoris.bookmarktype.AbstractBookmarkLabelProvider;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkFolder;

public class BookmarkFolderLabelProvider extends AbstractBookmarkLabelProvider {

	@Override
	public Image getImage(Object element) {
		String imageKey = ISharedImages.IMG_OBJ_FOLDER;
		return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
	}
	
	@Override
	public boolean handlesBookmark(Bookmark bookmark) {
		return bookmark instanceof BookmarkFolder;
	}

}
