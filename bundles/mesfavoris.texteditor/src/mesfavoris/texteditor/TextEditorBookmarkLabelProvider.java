package mesfavoris.texteditor;

import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_FILE_PATH;

import org.chabanois.mesfavoris.bookmarktype.AbstractBookmarkLabelProvider;
import org.chabanois.mesfavoris.model.Bookmark;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;

import mesfavoris.texteditor.placeholders.PathPlaceholderResolver;

public class TextEditorBookmarkLabelProvider extends AbstractBookmarkLabelProvider {
	private final PathPlaceholderResolver pathPlaceholderResolver;
	private final IEditorRegistry editorRegistry;
	private final ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());
	
	public TextEditorBookmarkLabelProvider() {
		editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
		pathPlaceholderResolver = new PathPlaceholderResolver(Activator.getPathPlaceholdersStore());
	}

	@Override
	public Image getImage(Object element) {
		Bookmark bookmark = (Bookmark) element;
		String pathValue = bookmark.getPropertyValue(PROP_FILE_PATH);
		IPath path = pathPlaceholderResolver.expand(pathValue);
		ImageDescriptor imageDescriptor = editorRegistry.getImageDescriptor(path.lastSegment());
		return resourceManager.createImage(imageDescriptor);
	}

	@Override
	public void dispose() {
		resourceManager.dispose();
		super.dispose();
	}
	
	@Override
	public boolean handlesBookmark(Bookmark bookmark) {
		return bookmark.getPropertyValue(PROP_FILE_PATH) != null;
	}

}
