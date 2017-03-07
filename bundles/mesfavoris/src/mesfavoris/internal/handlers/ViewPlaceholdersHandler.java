package mesfavoris.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.handlers.HandlerUtil;

import mesfavoris.internal.Constants;

public class ViewPlaceholdersHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow activeWorkbenchWindow = HandlerUtil
				.getActiveWorkbenchWindow(event);

		final Shell shell;
		if (activeWorkbenchWindow == null) {
			shell = null;
		} else {
			shell = activeWorkbenchWindow.getShell();
		}
		showPreferencePage(shell, Constants.PLACEHOLDERS_PREFERENCE_PAGE_ID);
		return null;
	}

	private static void showPreferencePage(Shell shell, String id) {
		PreferencesUtil.createPreferenceDialogOn(shell, id, new String[] {id}, null).open();
	}	
	
}
