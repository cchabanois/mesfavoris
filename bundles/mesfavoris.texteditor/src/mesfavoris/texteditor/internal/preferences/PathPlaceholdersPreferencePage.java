package mesfavoris.texteditor.internal.preferences;

import static mesfavoris.texteditor.internal.Constants.PLACEHOLDER_HOME_NAME;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import mesfavoris.BookmarksPlugin;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.remote.RemoteBookmarksStoreManager;
import mesfavoris.texteditor.Activator;
import mesfavoris.texteditor.internal.placeholders.usage.PathPlaceholderUsageDialog;
import mesfavoris.texteditor.placeholders.PathPlaceholder;
import mesfavoris.texteditor.placeholders.PathPlaceholdersStore;

public class PathPlaceholdersPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private TableViewer placeholdersTableViewer;
	private Button addButton;
	private Button editButton;
	private Button applyButton;
	private Button removeButton;
	private PathPlaceholdersStore pathPlaceholdersStore;
	private BookmarkDatabase bookmarkDatabase;
	private RemoteBookmarksStoreManager remoteBookmarksStoreManager;
	private PathPlaceholderStats pathPlaceholderStats;
	
	@Override
	public void init(IWorkbench workbench) {
		pathPlaceholdersStore = Activator.getPathPlaceholdersStore();
		bookmarkDatabase = BookmarksPlugin.getBookmarkDatabase();
		remoteBookmarksStoreManager = BookmarksPlugin.getRemoteBookmarksStoreManager();
	}

	@Override
	protected Control createContents(Composite parent) {
		// Create main composite
		Composite mainComposite = createComposite(parent, 2, 1, GridData.FILL_BOTH);

		createPathPlaceholdersTable(mainComposite);
		createTableButtons(mainComposite);
		updateButtonEnablement();

		placeholdersTableViewer.setInput(pathPlaceholdersStore);

		Dialog.applyDialogFont(mainComposite);
		return mainComposite;
	}

	private static Composite createComposite(Composite parent, int columns, int hspan, int fill) {
		Composite g = new Composite(parent, SWT.NONE);
		g.setLayout(new GridLayout(columns, false));
		g.setFont(parent.getFont());
		GridData gd = new GridData(fill);
		gd.horizontalSpan = hspan;
		g.setLayoutData(gd);
		return g;
	}

	private static Label createLabel(Composite parent, String text, int hspan) {
		Label l = new Label(parent, SWT.NONE);
		l.setFont(parent.getFont());
		l.setText(text);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = hspan;
		gd.grabExcessHorizontalSpace = false;
		l.setLayoutData(gd);
		return l;
	}

	private static Composite createComposite(Composite parent, Font font, int columns, int hspan, int fill,
			int marginwidth, int marginheight) {
		Composite g = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(columns, false);
		layout.marginWidth = marginwidth;
		layout.marginHeight = marginheight;
		g.setLayout(layout);
		g.setFont(font);
		GridData gd = new GridData(fill);
		gd.horizontalSpan = hspan;
		g.setLayoutData(gd);
		return g;
	}

	private static Button createPushButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.PUSH);
		button.setFont(parent.getFont());
		if (label != null) {
			button.setText(label);
		}
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		button.setLayoutData(gd);
		return button;
	}

	protected void createPathPlaceholdersTable(Composite parent) {
		Font font = parent.getFont();
		// Create label, add it to the parent to align the right side buttons
		// with the top of the table
		createLabel(parent, "Placeholder variables to &set:", 2);
		Composite tableComposite = createComposite(parent, font, 1, 1, GridData.FILL_BOTH, 0, 0);
		placeholdersTableViewer = new TableViewer(tableComposite,
				SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		Table table = placeholdersTableViewer.getTable();
		table.setLayout(new GridLayout());
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setFont(font);
		placeholdersTableViewer.setContentProvider(new PathPlaceholderContentProvider());
		pathPlaceholderStats = new PathPlaceholderStats(() -> bookmarkDatabase.getBookmarksTree());
		PathPlaceholderTableLabelProvider pathPlaceholderTableLabelProvider = new PathPlaceholderTableLabelProvider(
				pathPlaceholderStats);
		placeholdersTableViewer
				.setLabelProvider(new DelegatingStyledCellLabelProvider(pathPlaceholderTableLabelProvider));
		placeholdersTableViewer.setSorter(new PathPlaceholderViewerSorter());
		placeholdersTableViewer.addSelectionChangedListener(event -> updateButtonEnablement());
	}

	private void createTableButtons(Composite parent) {
		// Create button composite
		Composite buttonComposite = createComposite(parent, parent.getFont(), 1, 1,
				GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_END, 0, 0);

		// Create buttons
		addButton = createPushButton(buttonComposite, "N&ew...");
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				handleNewButtonSelected();
			}
		});
		editButton = createPushButton(buttonComposite, "E&dit...");
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				handleEditButtonSelected();
			}
		});
		editButton.setEnabled(false);
		applyButton = createPushButton(buttonComposite, "A&pply...");
		applyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				handleApplyButtonSelected();
			}
		});
		applyButton.setEnabled(false);
		removeButton = createPushButton(buttonComposite, "Rem&ove");
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				handleRemoveButtonSelected();
			}
		});
		removeButton.setEnabled(false);
	}

	protected void handleEditButtonSelected() {
		PathPlaceholderCreationDialog dialog = new PathPlaceholderCreationDialog(getShell(), pathPlaceholdersStore,
				getSelectedPathPlaceholder());
		if (dialog.open() != Window.OK) {
			return;
		}
		pathPlaceholdersStore.add(dialog.getPathPlaceholder());
		refresh();
	}

	protected void handleRemoveButtonSelected() {
		pathPlaceholdersStore.remove(getSelectedPathPlaceholder().getName());
		refresh();
	}

	private void refresh() {
		pathPlaceholderStats.refresh();
		placeholdersTableViewer.refresh();
	}
	
	protected void handleNewButtonSelected() {
		PathPlaceholderCreationDialog dialog = new PathPlaceholderCreationDialog(getShell(), pathPlaceholdersStore,
				null);
		if (dialog.open() != Window.OK) {
			return;
		}
		pathPlaceholdersStore.add(dialog.getPathPlaceholder());
		refresh();
	}

	private void handleApplyButtonSelected() {
		PathPlaceholderUsageDialog pathPlaceholderUsageDialog = new PathPlaceholderUsageDialog(getShell(),
				bookmarkDatabase, remoteBookmarksStoreManager, pathPlaceholdersStore,
				getSelectedPathPlaceholder().getName());
		pathPlaceholderUsageDialog.open();
		refresh();
	}

	private PathPlaceholder getSelectedPathPlaceholder() {
		IStructuredSelection structuredSelection = (IStructuredSelection) placeholdersTableViewer.getSelection();
		if (structuredSelection.isEmpty()) {
			return null;
		}
		return (PathPlaceholder) structuredSelection.getFirstElement();
	}

	private void updateButtonEnablement() {
		PathPlaceholder pathPlaceholder = getSelectedPathPlaceholder();
		editButton.setEnabled(pathPlaceholder != null && !isUnmodifiable(pathPlaceholder));
		applyButton.setEnabled(pathPlaceholder != null);
		removeButton.setEnabled(pathPlaceholder != null && !isUnmodifiable(pathPlaceholder));
	}

	private boolean isUnmodifiable(PathPlaceholder pathPlaceholder) {
		return PLACEHOLDER_HOME_NAME.equals(pathPlaceholder.getName());
	}

}
