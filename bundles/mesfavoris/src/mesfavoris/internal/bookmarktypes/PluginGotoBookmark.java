package mesfavoris.internal.bookmarktypes;

import java.util.List;

import org.eclipse.ui.IWorkbenchWindow;

import mesfavoris.bookmarktype.IBookmarkLocation;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.model.Bookmark;

public class PluginGotoBookmark implements IGotoBookmark {
	private final PluginBookmarkTypes pluginBookmarkTypes;
	private GotoBookmark gotoBookmark;
	
	public PluginGotoBookmark(PluginBookmarkTypes pluginBookmarkTypes) {
		this.pluginBookmarkTypes = pluginBookmarkTypes;
	}

	private synchronized GotoBookmark getGotoBookmark() {
		if (gotoBookmark != null) {
			return gotoBookmark;
		}
		List<IGotoBookmark> gotoBookmarks = pluginBookmarkTypes.getGotoBookmarks();
		this.gotoBookmark = new GotoBookmark(gotoBookmarks);
		return gotoBookmark;
	}
	
	@Override
	public boolean gotoBookmark(IWorkbenchWindow window, Bookmark bookmark, IBookmarkLocation bookmarkLocation) {
		return getGotoBookmark().gotoBookmark(window, bookmark, bookmarkLocation);
	}

}
