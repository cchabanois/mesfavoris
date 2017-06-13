package mesfavoris.texteditor.internal;

import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_FILE_PATH;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;

import mesfavoris.bookmarktype.AbstractBookmarkLabelProvider;
import mesfavoris.model.Bookmark;

public class TextEditorBookmarkLabelProvider extends AbstractBookmarkLabelProvider {
	private final IEditorRegistry editorRegistry;
	
	public TextEditorBookmarkLabelProvider() {
		editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
	}

	@Override
	public Image getImage(Object element) {
		Bookmark bookmark = (Bookmark) element;
		String pathValue = bookmark.getPropertyValue(PROP_FILE_PATH);
		// we do not need to use pathPlaceholderResolver because we are only interested by the fileName
		String fileName = getFileName(pathValue);
		ImageDescriptor imageDescriptor = editorRegistry.getImageDescriptor(fileName);
		return resourceManager.createImage(imageDescriptor);
	}

	private String getFileName(String path) {
		int index = path.lastIndexOf('/')+1;
		if (index == -1) {
			return null;
		}
		return path.substring(index);
	}
	
	@Override
	public boolean handlesBookmark(Bookmark bookmark) {
		return bookmark.getPropertyValue(PROP_FILE_PATH) != null;
	}

}
