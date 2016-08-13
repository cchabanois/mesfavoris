package mesfavoris.gdrive.dialogs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ShareBookmarksFileDialog extends TitleAreaDialog {
	private final Pattern simpleEmailPattern = Pattern.compile("^.+@.+\\..+$");
	private Text emailText;
	private Combo roleCombo;
	private String email = "";
	private boolean canWrite = true;
	
	public ShareBookmarksFileDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		setPageComplete(false);
		return control;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(dialogArea, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		setTitle("Share Bookmarks file");
		getShell().setText("Share Bookmarks file");
		setMessage("Share bookmarks file with others");	
		
		GridLayout gridLayout = new GridLayout(3, false);
		composite.setLayout(gridLayout);
		
		new Label(composite, SWT.NONE).setText("Email address :");
		emailText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		emailText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		emailText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				email = emailText.getText();
				validate();
			}
		});
		roleCombo = new Combo(composite, SWT.BORDER |SWT.READ_ONLY);
		roleCombo.add("Can Edit");
		roleCombo.add("Can View");
		roleCombo.select(0);
		roleCombo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				canWrite = roleCombo.getSelectionIndex() == 0;
				validate();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		Dialog.applyDialogFont(composite);
		return dialogArea;
	}
	
	private void validate() {
		boolean isValidEmail = isValidEmail(email);
		if (isValidEmail) {
			setErrorMessage(null);
			setPageComplete(true);
		} else {
			setErrorMessage("Not a valid email address");
			setPageComplete(false);
		}
	}

	private boolean isValidEmail(String email) {
		Matcher matcher = simpleEmailPattern.matcher(email);
		return matcher.matches();
	}
	
	private void setPageComplete(boolean complete) {
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null) {
			okButton.setEnabled(complete);
		}
	}	
	
	public String getEmail() {
		return email;
	}
	
	public boolean canWrite() {
		return canWrite;
	}
	
}
