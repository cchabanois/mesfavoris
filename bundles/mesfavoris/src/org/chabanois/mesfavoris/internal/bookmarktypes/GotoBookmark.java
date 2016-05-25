package org.chabanois.mesfavoris.internal.bookmarktypes;

import java.util.ArrayList;
import java.util.List;

import org.chabanois.mesfavoris.bookmarktype.IGotoBookmark;
import org.chabanois.mesfavoris.model.Bookmark;
import org.eclipse.ui.IWorkbenchWindow;

public class GotoBookmark implements IGotoBookmark {
	private final List<IGotoBookmark> gotoBookmarks;

	public GotoBookmark(List<IGotoBookmark> gotoBookmarks) {
		this.gotoBookmarks = new ArrayList<IGotoBookmark>(gotoBookmarks);
	}

	@Override
	public boolean gotoBookmark(IWorkbenchWindow window, Bookmark bookmark) {
		for (IGotoBookmark gotoBookmark : gotoBookmarks) {
			if (gotoBookmark.gotoBookmark(window, bookmark)) {
				return true;
			}
		}
		return false;
	}

}
