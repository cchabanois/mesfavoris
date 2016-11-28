package mesfavoris.texteditor.internal;

import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_FOLDER_PATH;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.graphics.Image;

import mesfavoris.bookmarktype.AbstractBookmarkLabelProvider;
import mesfavoris.model.Bookmark;
import mesfavoris.texteditor.Activator;

public class FolderBookmarkLabelProvider extends AbstractBookmarkLabelProvider {
	private final ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());
	
	public FolderBookmarkLabelProvider() {
	}

	@Override
	public Image getImage(Object element) {
		ImageDescriptor imageDescriptor = Activator.getImageDescriptor("icons/obj16/folder.png");
		return resourceManager.createImage(imageDescriptor);
	}
	
	@Override
	public void dispose() {
		resourceManager.dispose();
		super.dispose();
	}
	
	@Override
	public boolean handlesBookmark(Bookmark bookmark) {
		return bookmark.getPropertyValue(PROP_FOLDER_PATH) != null;
	}
}
