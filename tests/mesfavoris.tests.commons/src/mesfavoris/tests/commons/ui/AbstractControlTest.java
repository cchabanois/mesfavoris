package mesfavoris.tests.commons.ui;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.junit.After;
import org.junit.Before;

/**
 * Abstract class used to test control in isolation.
 * 
 * Call {@link #openShell(IControlCreator)} in your test method and use bot to
 * make assertions
 * 
 * @author cchabanois
 * 
 */
public class AbstractControlTest {
	protected SWTWorkbenchBot bot;

	@Before
	public void setupEclipseBot() {
		bot = new SWTWorkbenchBot();
	}

	@After
	public void closeDialog() {
		try {
			bot.shell(getClass().getSimpleName()).close();
		} catch (Exception e) {

		}
	}

	public void activateShell() {
		bot.shell(getClass().getSimpleName()).activate();
	}

	protected void openShell(final IControlCreator createControl) {
		openShell(createControl, 200, 50);
	}

	protected void openShell(final IControlCreator createControl, final int width, final int height) {
		UIThreadRunnable.asyncExec(() -> {
			Shell shell = new Shell(Display.getCurrent());
			shell.setText(AbstractControlTest.this.getClass().getSimpleName());
			shell.setBounds(100, 100, width, height);
			shell.setLayout(new FillLayout());
			createControl.createControl(shell);
			shell.open();
		});
		bot.shell(getClass().getSimpleName()).activate();
	}

	protected interface IControlCreator {
		public void createControl(Shell shell);

	}

}
