package mesfavoris.gdrive.preferences;

import java.io.IOException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
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

public class GDriveBookmarksStorePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private Button deleteCredentialsButton;
	private GDriveConnectionManager gDriveConnectionManager;
	private IBookmarkMappings bookmarkMappings;

	@Override
	public void init(IWorkbench workbench) {
		gDriveConnectionManager = Activator.getGDriveConnectionManager();
		bookmarkMappings = Activator.getBookmarkMappingsStore();
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, false));
		comp.setFont(parent.getFont());
		GridData gd = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(gd);

		createDeleteCredentialsGroup(comp);
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
		deleteCredentialsButton.setText("Delete credentials");
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

	private void deleteCredentials() {
		DeleteFileDataStoreOperation operation = new DeleteFileDataStoreOperation(
				gDriveConnectionManager.getDataStoreDir(), gDriveConnectionManager, bookmarkMappings);
		try {
			operation.deleteDefaultFileDataStore();
			MessageDialog.openInformation(getShell(), "Delete credentials",
					"Credentials file has been successfully deleted.");
		} catch (IOException e) {
			StatusHelper.showError("Could not delete credentials", e, true);
		}
	}

}
