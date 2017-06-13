package mesfavoris.bookmarktype;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;

import mesfavoris.model.Bookmark;

public abstract class AbstractBookmarkLabelProvider extends LabelProvider
		implements IBookmarkLabelProvider {
	protected final ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());
	
	@Override
	public void dispose() {
		resourceManager.dispose();
		super.dispose();
	}	
	
	@Override
	public StyledString getStyledText(Object element) {
		Bookmark bookmark = (Bookmark) element;
		String name = bookmark.getPropertyValue(Bookmark.PROPERTY_NAME);
		if (name == null) {
			name = "unnamed";
		}
		return new StyledString(name);
	}	
	
}
