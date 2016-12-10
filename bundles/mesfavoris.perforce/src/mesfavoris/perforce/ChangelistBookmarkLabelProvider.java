package mesfavoris.perforce;

import static mesfavoris.perforce.PerforceProjectProperties.PROP_CHANGELIST;

import org.eclipse.swt.graphics.Image;

import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;

import mesfavoris.bookmarktype.AbstractBookmarkLabelProvider;
import mesfavoris.model.Bookmark;

public class ChangelistBookmarkLabelProvider extends AbstractBookmarkLabelProvider {
	
	@Override
	public Image getImage(Object element) {
		return PerforceUIPlugin.getImage(IPerforceUIConstants.ICON_PATH+IPerforceUIConstants.IMG_CHG_SUBMITTED);
	}
	
	@Override
	public boolean handlesBookmark(Bookmark bookmark) {
		return bookmark.getPropertyValue(PROP_CHANGELIST) != null;
	}

}
