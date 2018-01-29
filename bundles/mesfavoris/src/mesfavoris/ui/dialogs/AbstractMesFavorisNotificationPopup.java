package mesfavoris.ui.dialogs;

import org.eclipse.mylyn.commons.ui.dialogs.AbstractNotificationPopup;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.IUIConstants;

public abstract class AbstractMesFavorisNotificationPopup extends AbstractNotificationPopup {

	public AbstractMesFavorisNotificationPopup(Display display) {
		super(display);
		setDelayClose(3000);
	}

	@Override
	protected String getPopupShellTitle() {
		return "Mes Favoris";
	}

	@Override
	protected Image getPopupShellImage(int maximumHeight) {
		return BookmarksPlugin.getDefault().getImageRegistry().get(IUIConstants.IMG_BOOKMARKS);
	}

}