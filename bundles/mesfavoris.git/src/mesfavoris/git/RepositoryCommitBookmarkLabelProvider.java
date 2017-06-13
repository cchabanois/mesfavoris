package mesfavoris.git;

import static mesfavoris.git.GitBookmarkProperties.PROP_COMMIT_ID;

import org.eclipse.egit.ui.internal.UIIcons;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.graphics.Image;

import mesfavoris.bookmarktype.AbstractBookmarkLabelProvider;
import mesfavoris.model.Bookmark;

public class RepositoryCommitBookmarkLabelProvider extends AbstractBookmarkLabelProvider {
	
	@Override
	public Image getImage(Object element) {
		return UIIcons.getImage(resourceManager, UIIcons.CHANGESET);
	}
	
	@Override
	public boolean handlesBookmark(Bookmark bookmark) {
		return bookmark.getPropertyValue(PROP_COMMIT_ID) != null;
	}

}
