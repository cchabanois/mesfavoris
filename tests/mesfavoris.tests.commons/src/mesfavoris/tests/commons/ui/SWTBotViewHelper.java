package mesfavoris.tests.commons.ui;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class SWTBotViewHelper {

	public static IViewPart showView(final String viewId) {
		return UIThreadRunnable.syncExec(() -> {
			try {
				IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IViewPart viewPart = activePage.showView(viewId);
				return viewPart;
			} catch (PartInitException e) {
				throw new RuntimeException(e);
			}
		});
	}

	public static void closeWelcomeView() {
		try {
			SWTWorkbenchBot bot = new SWTWorkbenchBot();
			bot.viewByTitle("Welcome").close();
		} catch (WidgetNotFoundException e) {
		}
	}
	
}
