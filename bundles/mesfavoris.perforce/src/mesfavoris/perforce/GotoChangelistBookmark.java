package mesfavoris.perforce;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

import com.perforce.team.ui.changelists.ChangelistEditorInput;

import mesfavoris.bookmarktype.IBookmarkLocation;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.model.Bookmark;

public class GotoChangelistBookmark implements IGotoBookmark {

	private static final String EDITOR_ID = "com.perforce.team.ui.changelists.ChangelistEditor";

	@Override
	public boolean gotoBookmark(IWorkbenchWindow window, Bookmark bookmark, IBookmarkLocation bookmarkLocation) {
		if (!(bookmarkLocation instanceof ChangelistBookmarkLocation)) {
			return false;
		}
		ChangelistBookmarkLocation changelistBookmarkLocation = (ChangelistBookmarkLocation) bookmarkLocation;
		ChangelistEditorInput input = new ChangelistEditorInput(changelistBookmarkLocation.getChangelist());

		try {
			IDE.openEditor(window.getActivePage(), input, EDITOR_ID, true);
		} catch (PartInitException e) {
			return false;
		}
		return true;
	}

}
