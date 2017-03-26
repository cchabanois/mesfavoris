package mesfavoris.tests.commons.ui;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withStyle;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withTooltip;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarPushButton;
import org.eclipse.ui.forms.widgets.Form;
import org.hamcrest.Matcher;
import org.hamcrest.SelfDescribing;

public class SWTBotForm extends AbstractSWTBot<Form> {

	public SWTBotForm(Form form, SelfDescribing description) throws WidgetNotFoundException {
		super(form, description);
	}

	public SWTBotForm(Form form) throws WidgetNotFoundException {
		this(form, null);
	}

	public String getMessage() {
		return widget.getMessage();
	}

	public SWTBotToolbarButton toolbarButtonWithTooltip(String tooltip, int index) {
		Matcher<ToolItem> matcher = allOf(widgetOfType(ToolItem.class), withTooltip(tooltip),
				withStyle(SWT.PUSH, "SWT.PUSH"));
		return new SWTBotToolbarPushButton((ToolItem) bot().widget(matcher, index), matcher);
	}

	public SWTBot bot() {
		return new SWTBot(widget);
	}

}
