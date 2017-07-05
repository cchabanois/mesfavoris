package mesfavoris.perforce;

import static mesfavoris.perforce.PerforceBookmarkProperties.PROP_CHANGELIST;

import org.eclipse.jface.resource.ImageDescriptor;

import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;

import mesfavoris.bookmarktype.AbstractBookmarkLabelProvider;
import mesfavoris.model.Bookmark;

public class ChangelistBookmarkLabelProvider extends AbstractBookmarkLabelProvider {
	
	@Override
	public ImageDescriptor getImageDescriptor(Context context, Bookmark bookmark) {
		return PerforceUIPlugin.getDescriptor(IPerforceUIConstants.IMG_CHG_SUBMITTED);
	}
	
	@Override
	public boolean canHandle(Context context, Bookmark bookmark) {
		return bookmark.getPropertyValue(PROP_CHANGELIST) != null;
	}

}
