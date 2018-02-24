package mesfavoris.tests.commons.ui;


import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.ui.forms.widgets.Form;
import org.hamcrest.Matcher;

import mesfavoris.MesFavoris;

public class BookmarksViewDriver {
	private final SWTWorkbenchBot bot;
	
	public BookmarksViewDriver(SWTWorkbenchBot bot) {
		this.bot = bot;
	}
	
	public void showView() {
		SWTBotViewHelper.showView(MesFavoris.VIEW_ID);
	}
	
	public SWTBotView view() {
		return bot.viewById(MesFavoris.VIEW_ID);
	}
	
	public SWTBotTree tree() throws WidgetNotFoundException {
		return view().bot().tree();
	}
	
	public SWTBotForm form() {
		Matcher<Form> matcher = widgetOfType(Form.class);
		return new SWTBotForm(view().bot().widget(matcher), matcher);
	}
	
}
