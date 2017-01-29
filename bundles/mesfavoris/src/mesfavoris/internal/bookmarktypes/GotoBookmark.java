package mesfavoris.internal.bookmarktypes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.ui.IWorkbenchWindow;

import mesfavoris.bookmarktype.IBookmarkLocation;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.internal.StatusHelper;
import mesfavoris.model.Bookmark;

public class GotoBookmark implements IGotoBookmark {

	private final List<IGotoBookmark> gotoBookmarks;

	public GotoBookmark(List<IGotoBookmark> gotoBookmarks) {
		this.gotoBookmarks = new ArrayList<IGotoBookmark>(gotoBookmarks);
	}

	@Override
	public boolean gotoBookmark(IWorkbenchWindow window, Bookmark bookmark, IBookmarkLocation bookmarkLocation) {
		for (IGotoBookmark gotoBookmark : gotoBookmarks) {
			if (gotoBookmark(gotoBookmark, window, bookmark, bookmarkLocation)) {
				return true;
			}
		}
		return false;
	}

	private boolean gotoBookmark(IGotoBookmark gotoBookmark, IWorkbenchWindow window, Bookmark bookmark,
			IBookmarkLocation bookmarkLocation) {
		final boolean[] result = new boolean[] { false };
		SafeRunner.run(new ISafeRunnable() {

			public void run() throws Exception {
				result[0] = gotoBookmark.gotoBookmark(window, bookmark, bookmarkLocation);
			}

			public void handleException(Throwable exception) {
				StatusHelper.logError("Error during gotoBookmark", exception);
			}
		});
		return result[0];
	}

}
