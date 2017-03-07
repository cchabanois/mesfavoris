package mesfavoris.internal.problems.handlers;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

import mesfavoris.BookmarksException;
import mesfavoris.internal.Constants;
import mesfavoris.problems.BookmarkProblem;
import mesfavoris.problems.IBookmarkProblemHandler;

public class LocalPathSharedProblemHandler implements IBookmarkProblemHandler {

	@Override
	public String getErrorMessage(BookmarkProblem bookmarkProblem) {
		return "Some properties are using local paths";
	}

	@Override
	public String getActionMessage(BookmarkProblem bookmarkProblem) {
		return "Set placeholder variables";
	}

	@Override
	public void handleAction(BookmarkProblem bookmarkProblem) throws BookmarksException {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		showPreferencePage(shell, Constants.PLACEHOLDERS_PREFERENCE_PAGE_ID);
	}

	private static void showPreferencePage(Shell shell, String id) {
		PreferencesUtil.createPreferenceDialogOn(shell, id, new String[] {id}, null).open();
	}	
	
}
