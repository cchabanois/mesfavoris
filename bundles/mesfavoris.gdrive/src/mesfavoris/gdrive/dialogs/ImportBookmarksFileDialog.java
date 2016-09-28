package mesfavoris.gdrive.dialogs;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import com.google.api.client.util.Lists;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import mesfavoris.gdrive.mappings.IBookmarkMappings;
import mesfavoris.gdrive.operations.AddFileToFolderOperation;
import mesfavoris.gdrive.operations.GetBookmarkFilesOperation;

public class ImportBookmarksFileDialog extends TitleAreaDialog {
	private FileTableViewer fileTableViewer;
	private final Drive drive;
	private final String applicationFolderId;
	private final IBookmarkMappings bookmarkMappings;
	private File file;

	public ImportBookmarksFileDialog(Shell parentShell, Drive drive, String applicationFolderId,
			IBookmarkMappings bookmarkMappings) {
		super(parentShell);
		this.drive = drive;
		this.applicationFolderId = applicationFolderId;
		this.bookmarkMappings = bookmarkMappings;
	}

	@Override
	protected Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		setPageComplete(false);
		refreshFiles();
		return control;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(dialogArea, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		setTitle("Import Bookmarks file");
		getShell().setText("Import Bookmarks file");
		setMessage("Import a bookmarks file");

		GridLayout gridLayout = new GridLayout(2, false);
		composite.setLayout(gridLayout);

		createFileTableViewer(composite);
		createAddLinkButton(composite);
		Dialog.applyDialogFont(composite);
		return dialogArea;
	}

	private void createFileTableViewer(Composite parent) {
		fileTableViewer = new FileTableViewer(parent, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 100;
		gd.widthHint = 100;
		fileTableViewer.getTable().setLayoutData(gd);
		fileTableViewer.setFiles(Lists.newArrayList());
		fileTableViewer.addSelectionChangedListener(event -> {
			IStructuredSelection selection = (IStructuredSelection) fileTableViewer.getSelection();
			file = (File) selection.getFirstElement();
			setPageComplete(file != null);
		});
		fileTableViewer.setFilters(new ViewerFilter[] { new ViewerFilter() {

			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				File file = (File) element;
				return !bookmarkMappings.getMapping(file.getId()).isPresent();
			}
		} });
	}

	private void createAddLinkButton(Composite parent) {
		Button button = new Button(parent, SWT.NONE);
		button.setText("Add link...");
		GridData gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		button.setLayoutData(gd);
		button.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				AddGDriveLinkUrlDialog dialog = new AddGDriveLinkUrlDialog(getShell());
				if (dialog.open() == Window.OK) {
					AddFileToFolderOperation operation = new AddFileToFolderOperation(drive);
					try {
						operation.addToFolder(applicationFolderId, dialog.getFileId());
					} catch (IOException e) {

					}
					refreshFiles();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	private void setPageComplete(boolean complete) {
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null) {
			okButton.setEnabled(complete);
		}
	}

	private void refreshFiles() {
		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		try {
			progressService.busyCursorWhile(monitor -> {
				List<File> bookmarkFiles = Lists.newArrayList();
				try {
					GetBookmarkFilesOperation operation = new GetBookmarkFilesOperation(drive);
					bookmarkFiles.addAll(operation.getBookmarkFiles());
				} catch (IOException e) {
					throw new InvocationTargetException(e);
				} finally {
					Display.getDefault().asyncExec(() -> {
						fileTableViewer.setInput(bookmarkFiles);
					});
				}
			});
		} catch (InvocationTargetException e) {
			setMessage("Could not get bookmarks file : " + e.getCause().getMessage(), IMessageProvider.ERROR);
		} catch (InterruptedException e) {
			setMessage("Could not get bookmarks file", IMessageProvider.ERROR);
		}
	}

	public File getFile() {
		return file;
	}

}
