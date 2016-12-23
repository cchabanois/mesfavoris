package mesfavoris.internal.preferences.placeholders;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.IUIConstants;
import mesfavoris.placeholders.IPathPlaceholders;
import mesfavoris.placeholders.PathPlaceholder;

/**
 * Dialog to create/edit a {@link PathPlaceholder}
 * 
 * @author cchabanois
 *
 */
public class PathPlaceholderCreationDialog extends StatusDialog {
	private Text nameText;
	private Text pathText;
	private final IDialogSettings dialogSettings;
	private final PathPlaceholder initialPathPlaceholder;
	private PathPlaceholder pathPlaceholder;
	private final IPathPlaceholders pathPlaceholders;

	public PathPlaceholderCreationDialog(Shell parent, IPathPlaceholders pathPlaceholders,
			PathPlaceholder pathPlaceholder) {
		super(parent);
		this.pathPlaceholders = pathPlaceholders;
		this.initialPathPlaceholder = pathPlaceholder;
		this.pathPlaceholder = pathPlaceholder;
		if (pathPlaceholder == null) {
			setTitle("New Placeholder Entry");
		} else {
			setTitle("Edit Placeholder Entry");
		}
		dialogSettings = BookmarksPlugin.getDefault().getDialogSettings();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		Composite inner = new Composite(composite, SWT.NONE);
		inner.setFont(composite.getFont());

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 3;
		inner.setLayout(layout);

		int fieldWidthHint = convertWidthInCharsToPixels(50);

		Label nameLabel = new Label(inner, SWT.LEFT);
		nameLabel.setText("Name :");

		nameText = new Text(inner, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData();
		gd.widthHint = fieldWidthHint;
		gd.grabExcessHorizontalSpace = true;
		nameText.setLayoutData(gd);
		if (initialPathPlaceholder != null) {
			nameText.setText(initialPathPlaceholder.getName());
		}
		nameText.setEditable(initialPathPlaceholder == null);
		nameText.addModifyListener(e -> updateStatus());
		
		createEmptySpace(inner, 1);

		Label pathLabel = new Label(inner, SWT.LEFT);
		pathLabel.setText("Path :");

		pathText = new Text(inner, SWT.SINGLE | SWT.BORDER);
		gd = new GridData();
		gd.widthHint = fieldWidthHint;
		gd.grabExcessHorizontalSpace = true;
		pathText.setLayoutData(gd);
		if (initialPathPlaceholder != null) {
			pathText.setText(initialPathPlaceholder.getPath().toString());
		}	
		pathText.addModifyListener(e -> updateStatus());

		Button folderButton = new Button(inner, SWT.PUSH);
		folderButton.setText("Folder...");
		folderButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IPath path = chooseExtDirectory();
				pathText.setText(path.toOSString());
			}
		});
		
		applyDialogFont(composite);
		return composite;
	}

	private Control createEmptySpace(Composite parent, int span) {
		Label label = new Label(parent, SWT.LEFT);
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		gd.grabExcessHorizontalSpace = false;
		gd.horizontalSpan = span;
		gd.horizontalIndent = 0;
		gd.widthHint = 0;
		gd.heightHint = 0;
		label.setLayoutData(gd);
		return label;
	}

	private String getFilterPath() {
		String initPath = pathText.getText();
		if (initPath.length() == 0) {
			initPath = dialogSettings.get(IUIConstants.LAST_FOLDER);
			if (initPath == null) {
				initPath = "";
			}
		} else {
			IPath entryPath = new Path(initPath);
			initPath = entryPath.toOSString();
		}
		return initPath;
	}

	private IPath chooseExtDirectory() {
		String filterPath = getFilterPath();

		DirectoryDialog dialog = new DirectoryDialog(getShell());
		dialog.setText("Folder selection");
		dialog.setMessage("Specify the folder to be represented by the variable:");
		dialog.setFilterPath(filterPath);
		String res = dialog.open();
		if (res != null) {
			dialogSettings.put(IUIConstants.LAST_FOLDER, dialog.getFilterPath());
			return Path.fromOSString(res);
		}
		return null;
	}

	private void updateStatus() {
		IStatus status = getMoreSevere(getPathStatus(), getNameStatus());
		if (status.isOK() || status.getSeverity() == IStatus.WARNING) {
			pathPlaceholder = new PathPlaceholder(nameText.getText(), new Path(pathText.getText()));
		} else {
			pathPlaceholder = null;
		}
		updateStatus(status);
	}
	
	private IStatus getPathStatus() {
		String path = pathText.getText();
		IStatus status;
		if (path.length() == 0) {
			status = new Status(IStatus.OK, BookmarksPlugin.PLUGIN_ID, "");
		} else if (!Path.ROOT.isValidPath(path)) {
			status = new Status(IStatus.ERROR, BookmarksPlugin.PLUGIN_ID, "The path is invalid.");
		} else if (!new File(path).exists()) {
			status = new Status(IStatus.WARNING, BookmarksPlugin.PLUGIN_ID, "Path does not exist.");
		} else {
			status = new Status(IStatus.OK, BookmarksPlugin.PLUGIN_ID, "");
		}
		return status;
	}

	private IStatus getNameStatus() {
		String name = nameText.getText();
		IStatus status;
		if (name.length() == 0) {
			status = new Status(IStatus.ERROR, BookmarksPlugin.PLUGIN_ID, "Enter a variable name.");
		} else if (name.trim().length() != name.length()) {
			status = new Status(IStatus.ERROR, BookmarksPlugin.PLUGIN_ID,
					"The variable name starts or ends with white spaces.");
		} else if (!Path.ROOT.isValidSegment(name)) {
			status = new Status(IStatus.ERROR, BookmarksPlugin.PLUGIN_ID, "The variable name contains ':', '/' or '\'.");
		} else if (nameConflict(name)) {
			status = new Status(IStatus.ERROR, BookmarksPlugin.PLUGIN_ID, "Variable name already exists.");
		} else {
			status = new Status(IStatus.OK, BookmarksPlugin.PLUGIN_ID, "");
		}
		return status;
	}

	private boolean nameConflict(String name) {
		if (initialPathPlaceholder != null && initialPathPlaceholder.getName().equals(name)) {
			return false;
		}
		return pathPlaceholders.get(name) != null;
	}

	public PathPlaceholder getPathPlaceholder() {
		return pathPlaceholder;
	}
	
	private static IStatus getMoreSevere(IStatus s1, IStatus s2) {
		if (s1.getSeverity() > s2.getSeverity()) {
			return s1;
		} else {
			return s2;
		}
	}
	
}
