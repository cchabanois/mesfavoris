package mesfavoris.git;

import static mesfavoris.git.GitBookmarkProperties.PROP_COMMIT_ID;

import org.eclipse.egit.ui.internal.UIIcons;
import org.eclipse.jface.resource.ImageDescriptor;

import mesfavoris.bookmarktype.AbstractBookmarkLabelProvider;
import mesfavoris.model.Bookmark;

public class RepositoryCommitBookmarkLabelProvider extends AbstractBookmarkLabelProvider {
	
	@Override
	public ImageDescriptor getImageDescriptor(Context context, Bookmark bookmark) {
		return UIIcons.CHANGESET;
	}
	
	@Override
	public boolean canHandle(Context context, Bookmark bookmark) {
		return bookmark.getPropertyValue(PROP_COMMIT_ID) != null;
	}

}
