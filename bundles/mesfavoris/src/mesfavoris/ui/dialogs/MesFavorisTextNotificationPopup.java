package mesfavoris.ui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

public class MesFavorisTextNotificationPopup extends AbstractMesFavorisNotificationPopup {
	private final String text;
	
	public MesFavorisTextNotificationPopup(Display display, String text) {
		super(display);
		this.text = text;
	}

	@Override
	protected void createContentArea(Composite composite) {
		composite.setLayout(new GridLayout(1, true));
		Label label = new Label(composite, SWT.WRAP);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		label.setText(this.text);
	}
}
