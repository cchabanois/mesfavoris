package mesfavoris.gdrive.preferences;

import static mesfavoris.gdrive.preferences.IPreferenceConstants.POLL_CHANGES_INTERVAL_PREF;

import java.io.IOException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import mesfavoris.gdrive.Activator;
import mesfavoris.gdrive.StatusHelper;
import mesfavoris.gdrive.connection.GDriveConnectionManager;
import mesfavoris.gdrive.mappings.IBookmarkMappings;
import mesfavoris.gdrive.operations.DeleteFileDataStoreOperation;
import mesfavoris.remote.UserInfo;

public class GDriveBookmarksStorePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private static final int MIN_POLL_CHANGES_INTERVAL = 30;
	private static final int MAX_POLL_CHANGES_INTERVAL = 9999;
	private Button deleteCredentialsButton;
	private GDriveConnectionManager gDriveConnectionManager;
	private IBookmarkMappings bookmarkMappings;
	private IntegerFieldEditor pollChangesInterval;

	@Override
	public void init(IWorkbench workbench) {
		gDriveConnectionManager = Activator.getDefault().getGDriveConnectionManager();
		bookmarkMappings = Activator.getDefault().getBookmarkMappingsStore();
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, false));
		comp.setFont(parent.getFont());
		GridData gd = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(gd);

		createDeleteCredentialsGroup(comp);
		createPollChangesIntervalGroup(comp);
		
		initialize();
		checkState();
		return comp;
	}

	private void createDeleteCredentialsGroup(Composite parent) {
		GridData gd;
		Group group = new Group(parent, SWT.NONE);
		group.setLayout(new GridLayout(1, false));
		group.setText("Delete credentials");
		group.setFont(parent.getFont());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);

		Label label = new Label(group, SWT.LEFT | SWT.WRAP);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 350;
		label.setLayoutData(gd);
		label.setText(
				"This will delete the file data store containing credentials. You will have to run the oauth flow again (possibly with another user)");
		label.setFont(parent.getFont());

		deleteCredentialsButton = new Button(group, SWT.PUSH);
		deleteCredentialsButton.setFont(parent.getFont());
		deleteCredentialsButton.setText(getDeleteCredentialsText(gDriveConnectionManager.getUserInfo()));
		gd = new GridData(SWT.BEGINNING);
		deleteCredentialsButton.setLayoutData(gd);

		deleteCredentialsButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				deleteCredentials();
			}
		});
	}

	private String getDeleteCredentialsText(UserInfo user) {
		if (user == null || user.getEmailAddress() == null) {
			return "Delete credentials";
		} else {
			return "Delete credentials for " + user.getEmailAddress();
		}
	}

	private void deleteCredentials() {
		DeleteFileDataStoreOperation operation = new DeleteFileDataStoreOperation(gDriveConnectionManager,
				bookmarkMappings);
		try {
			operation.deleteFileDataStore();
			MessageDialog.openInformation(getShell(), "Delete credentials",
					"Credentials file has been successfully deleted.");
		} catch (IOException e) {
			StatusHelper.showError("Could not delete credentials", e, true);
		} finally {
			deleteCredentialsButton.setText(getDeleteCredentialsText(gDriveConnectionManager.getUserInfo()));
		}
	}

	private void createPollChangesIntervalGroup(Composite composite) {
		Composite groupComposite = new Composite(composite, SWT.LEFT);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		groupComposite.setLayout(layout);
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		groupComposite.setLayoutData(gd);

		pollChangesInterval = new IntegerFieldEditor(POLL_CHANGES_INTERVAL_PREF, "Poll changes interval (seconds)",
				groupComposite);

		pollChangesInterval.setTextLimit(Integer.toString(MAX_POLL_CHANGES_INTERVAL).length());
		pollChangesInterval.setErrorMessage("The workspace save interval should be between 30 and 9999.");
		pollChangesInterval.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		pollChangesInterval.setValidRange(MIN_POLL_CHANGES_INTERVAL, MAX_POLL_CHANGES_INTERVAL);
		pollChangesInterval.setPropertyChangeListener(event -> {
			if (event.getProperty().equals(FieldEditor.IS_VALID)) {
				setValid(pollChangesInterval.isValid());
			}
		});
		addField(pollChangesInterval);
	}

	@Override
	protected void createFieldEditors() {
		//do nothing we overload the create contents method
	}
	
}
