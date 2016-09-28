package mesfavoris.gdrive.dialogs;

import java.util.Optional;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import mesfavoris.gdrive.operations.GetFileIdFromUrlOperation;

public class AddGDriveLinkUrlDialog extends TitleAreaDialog {
	private Text urlText;
	private String fileId;

	public AddGDriveLinkUrlDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	public void create() {
		super.create();
		setTitle("Add a link to a gdrive bookmarks file");
		getShell().setText("Add a link to a gdrive bookmarks file");
		setPageComplete(false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(1, false);
		container.setLayout(layout);

		createUrlText(container);
		Dialog.applyDialogFont(container);
		return area;
	}

	private void createUrlText(Composite container) {
		Label label = new Label(container, SWT.NONE);
		label.setText("Url : ");

		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;

		urlText = new Text(container, SWT.BORDER);
		urlText.setLayoutData(gd);
		urlText.addModifyListener(event -> {
			GetFileIdFromUrlOperation operation = new GetFileIdFromUrlOperation();
			Optional<String> fileId = operation.getFileId(urlText.getText());
			setPageComplete(fileId.isPresent());
			this.fileId = fileId.isPresent() ? fileId.get() : null;
			if (!fileId.isPresent()) {
				setErrorMessage("Url is not a valid gdrive url");
			} else {
				setErrorMessage(null);
			}
		});
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	public String getFileId() {
		return fileId;
	}

	private void setPageComplete(boolean complete) {
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null) {
			okButton.setEnabled(complete);
		}
	}
}