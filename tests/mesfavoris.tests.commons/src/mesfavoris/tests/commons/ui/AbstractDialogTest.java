package mesfavoris.tests.commons.ui;

import java.util.Arrays;
import java.util.function.Function;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.junit.After;
import org.junit.Before;

import mesfavoris.tests.commons.Activator;

public class AbstractDialogTest {
	protected SWTWorkbenchBot bot;
	private String shellText = "no shell text";

	@Before
	public void setupWorkbenchBot() {
		bot = new SWTWorkbenchBot();
	}

	@After
	public void closeDialog() {
		if (Arrays.stream(bot.shells()).filter(shell -> shellText.equals(shell.getText())).findAny().isPresent()) {
			bot.shell(shellText).close();
		}
	}

	public void activateShell() {
		bot.shell(shellText).activate();
	}

	protected void openDialog(Function<Shell, Dialog> dialogCreator) {
		UIThreadRunnable.syncExec(() -> {
			Shell parentShell = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
			Dialog dialog = dialogCreator.apply(parentShell);
			dialog.setBlockOnOpen(false);
			dialog.open();
			shellText = dialog.getShell().getText();
		});
		bot.shell(shellText).activate();
	}

	protected void clickOkButton() {
		bot.button("OK").click();
	}

}
