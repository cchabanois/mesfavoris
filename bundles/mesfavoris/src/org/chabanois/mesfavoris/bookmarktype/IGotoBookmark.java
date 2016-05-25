package org.chabanois.mesfavoris.bookmarktype;

import org.chabanois.mesfavoris.model.Bookmark;
import org.eclipse.ui.IWorkbenchWindow;

public interface IGotoBookmark {

	public boolean gotoBookmark(IWorkbenchWindow window, Bookmark bookmark);
	
}
