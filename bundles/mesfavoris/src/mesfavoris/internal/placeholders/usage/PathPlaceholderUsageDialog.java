package mesfavoris.internal.placeholders.usage;

import static mesfavoris.PathBookmarkProperties.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import mesfavoris.BookmarksException;
import mesfavoris.BookmarksPlugin;
import mesfavoris.bookmarktype.IBookmarkLabelProvider;
import mesfavoris.internal.StatusHelper;
import mesfavoris.internal.service.operations.CollapseBookmarksOperation;
import mesfavoris.internal.service.operations.ExpandBookmarksOperation;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.placeholders.IPathPlaceholders;
import mesfavoris.placeholders.PathPlaceholderResolver;
import mesfavoris.remote.RemoteBookmarksStoreManager;

public class PathPlaceholderUsageDialog extends TitleAreaDialog {
	private final BookmarkDatabase bookmarkDatabase;
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;
	private final IPathPlaceholders pathPlaceholders;
	private final String pathPlaceholderName;
	private final IBookmarkLabelProvider bookmarkLabelProvider;
	private TableViewer collapsableBookmarksViewer;
	private TableViewer collapsedBookmarksViewer;
	private Button addButton;
	private Button addAllButton;
	private Button removeButton;
	private Button removeAllButton;

	public PathPlaceholderUsageDialog(Shell parentShell, BookmarkDatabase bookmarkDatabase,
			RemoteBookmarksStoreManager remoteBookmarksStoreManager, IPathPlaceholders pathPlaceholders,
			String pathPlaceholderName) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.bookmarkDatabase = bookmarkDatabase;
		this.remoteBookmarksStoreManager = remoteBookmarksStoreManager;
		this.pathPlaceholders = pathPlaceholders;
		this.pathPlaceholderName = pathPlaceholderName;
		this.bookmarkLabelProvider = BookmarksPlugin.getBookmarkLabelProvider();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Path placeholder");
		setMessage("Path placeholder usage");

		Composite parentComposite = (Composite) super.createDialogArea(parent);

		Composite container = new Composite(parentComposite, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		container.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.makeColumnsEqualWidth = false;
		layout.horizontalSpacing = 5;
		layout.verticalSpacing = 10;
		container.setLayout(layout);

		createCollapsableBookmarksList(container).setLayoutData(new GridData(GridData.FILL_BOTH));
		createButtonArea(container);
		createCollapsedBookmarksList(container).setLayoutData(new GridData(GridData.FILL_BOTH));
		updateButtonEnablement();

		Dialog.applyDialogFont(parentComposite);
		return container;
	}

	private Composite createCollapsableBookmarksList(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		container.setLayout(layout);
		container.setLayoutData(new GridData());

		Label label = new Label(container, SWT.NONE);
		label.setText("Bookmarks with collapsable path :");

		Table table = new Table(container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 200;
		gd.widthHint = 225;
		table.setLayoutData(gd);

		collapsableBookmarksViewer = new TableViewer(table);
		BookmarksPathLabelProvider bookmarksLabelProvider = new BookmarksPathLabelProvider(bookmarkDatabase,
				remoteBookmarksStoreManager, bookmarkLabelProvider);
		collapsableBookmarksViewer.setLabelProvider(bookmarksLabelProvider);
		collapsableBookmarksViewer.setContentProvider(new CollapsableBookmarksContentProvider());
		collapsableBookmarksViewer.setInput(bookmarkDatabase);
		collapsableBookmarksViewer.addSelectionChangedListener(event -> updateButtonEnablement());

		return container;
	}

	private Composite createButtonArea(Composite parent) {
		ScrolledComposite comp = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		comp.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		Composite container = new Composite(comp, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 0;
		container.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		gd.verticalIndent = 15;
		container.setLayoutData(gd);

		addButton = new Button(container, SWT.PUSH);
		addButton.setText("&Add ->");
		addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleAddButtonSelected();
			}
		});

		addAllButton = new Button(container, SWT.PUSH);
		addAllButton.setText("A&dd All ->");
		addAllButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addAllButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleAddAllButtonSelected();
			}
		});

		removeButton = new Button(container, SWT.PUSH);
		removeButton.setText("<- &Remove");
		removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleRemoveButtonSelected();
			}
		});

		removeAllButton = new Button(container, SWT.PUSH);
		removeAllButton.setText("<- Re&move All");
		removeAllButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		removeAllButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleRemoveAllButtonSelected();
			}
		});

		comp.setContent(container);
		comp.setMinHeight(250);
		comp.setExpandHorizontal(true);
		comp.setExpandVertical(true);
		return container;
	}

	@SuppressWarnings("unchecked")
	private List<BookmarkId> getSelectedCollapsableBookmarks() {
		IStructuredSelection collapsableSelection = (IStructuredSelection) collapsableBookmarksViewer.getSelection();
		Iterable<Bookmark> iterable = () -> collapsableSelection.iterator();
		return StreamSupport.stream(iterable.spliterator(), false).map(bookmark -> bookmark.getId())
				.collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	private List<BookmarkId> getSelectedCollapsedBookmarks() {
		IStructuredSelection collapsableSelection = (IStructuredSelection) collapsedBookmarksViewer.getSelection();
		Iterable<Bookmark> iterable = () -> collapsableSelection.iterator();
		return StreamSupport.stream(iterable.spliterator(), false).map(bookmark -> bookmark.getId())
				.collect(Collectors.toList());
	}

	private List<BookmarkId> getModifiableBookmarks(List<BookmarkId> bookmarkIds) {
		return bookmarkIds.stream()
				.filter(bookmarkId -> bookmarkDatabase.getBookmarksModificationValidator()
						.validateModification(bookmarkDatabase.getBookmarksTree(), bookmarkId).isOK())
				.collect(Collectors.toList());
	}

	private void updateButtonEnablement() {
		List<BookmarkId> modifiableCollapsable = getModifiableBookmarks(getSelectedCollapsableBookmarks());
		List<BookmarkId> modifiableCollapsed = getModifiableBookmarks(getSelectedCollapsedBookmarks());

		addButton.setEnabled(!modifiableCollapsable.isEmpty());
		addAllButton.setEnabled(collapsableBookmarksViewer.getTable().getItemCount() > 0);
		removeButton.setEnabled(!modifiableCollapsed.isEmpty());
		removeAllButton.setEnabled(collapsedBookmarksViewer.getTable().getItemCount() > 0);
	}

	private void handleAddButtonSelected() {
		CollapseBookmarksOperation operation = new CollapseBookmarksOperation(bookmarkDatabase, pathPlaceholders);
		try {
			operation.collapse(getSelectedCollapsableBookmarks(), pathPlaceholderName);
			refresh();
		} catch (BookmarksException e) {
			StatusHelper.logError("Could not collapse selected bookmarks", e);
		}
	}

	private void handleAddAllButtonSelected() {
		CollapseBookmarksOperation operation = new CollapseBookmarksOperation(bookmarkDatabase, pathPlaceholders);
		List<BookmarkId> allBookmarkIds = getAllBookmarksFromTable(collapsableBookmarksViewer);
		try {
			operation.collapse(allBookmarkIds, pathPlaceholderName);
			refresh();
		} catch (BookmarksException e) {
			StatusHelper.logError("Could not collapse selected bookmarks", e);
		}
	}

	private List<BookmarkId> getAllBookmarksFromTable(TableViewer tableViewer) {
		return Arrays.stream(tableViewer.getTable().getItems()).map(tableItem -> (Bookmark) tableItem.getData())
				.map(bookmark -> bookmark.getId()).collect(Collectors.toList());
	}

	private void handleRemoveButtonSelected() {
		ExpandBookmarksOperation operation = new ExpandBookmarksOperation(bookmarkDatabase, pathPlaceholders);
		try {
			operation.expand(getSelectedCollapsedBookmarks());
			refresh();
		} catch (BookmarksException e) {
			StatusHelper.logError("Could not expand selected bookmarks", e);
		}
	}

	private void handleRemoveAllButtonSelected() {
		ExpandBookmarksOperation operation = new ExpandBookmarksOperation(bookmarkDatabase, pathPlaceholders);
		List<BookmarkId> allBookmarkIds = getAllBookmarksFromTable(collapsedBookmarksViewer);
		try {
			operation.expand(allBookmarkIds);
			refresh();
		} catch (BookmarksException e) {
			StatusHelper.logError("Could not expand selected bookmarks", e);
		}
	}

	private Composite createCollapsedBookmarksList(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		container.setLayout(layout);
		container.setLayoutData(new GridData());

		Label label = new Label(container, SWT.NONE);
		label.setText("Bookmarks with collapsed path :");

		Table table = new Table(container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 200;
		gd.widthHint = 225;
		table.setLayoutData(gd);

		collapsedBookmarksViewer = new TableViewer(table);
		BookmarksPathLabelProvider bookmarksLabelProvider = new BookmarksPathLabelProvider(bookmarkDatabase,
				remoteBookmarksStoreManager, bookmarkLabelProvider);
		collapsedBookmarksViewer.setLabelProvider(bookmarksLabelProvider);
		collapsedBookmarksViewer.setContentProvider(new CollapsedBookmarksContentProvider());
		collapsedBookmarksViewer.setInput(bookmarkDatabase);
		collapsedBookmarksViewer.addSelectionChangedListener(event -> updateButtonEnablement());

		return container;
	}

	private void refresh() {
		collapsableBookmarksViewer.refresh();
		collapsedBookmarksViewer.refresh();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	private final class CollapsableBookmarksContentProvider implements IStructuredContentProvider {

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public Object[] getElements(Object inputElement) {
			BookmarkDatabase bookmarkDatabase = (BookmarkDatabase) inputElement;
			CollapsableBookmarksProvider collapsableBookmarksProvider = new CollapsableBookmarksProvider(
					new PathPlaceholderResolver(pathPlaceholders), pathPlaceholderName);

			return collapsableBookmarksProvider.getCollapsableBookmarks(bookmarkDatabase.getBookmarksTree()).toArray();
		}

	}

	private final class CollapsedBookmarksContentProvider implements IStructuredContentProvider {

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public Object[] getElements(Object inputElement) {
			BookmarkDatabase bookmarkDatabase = (BookmarkDatabase) inputElement;

			List<Bookmark> collapsedBookmarks = StreamSupport
					.stream(bookmarkDatabase.getBookmarksTree().spliterator(), false)
					.filter(bookmark -> bookmark.getPropertyValue(PROP_FILE_PATH) != null
							&& pathPlaceholderName.equals(PathPlaceholderResolver.getPlaceholderName(
									bookmark.getPropertyValue(PROP_FILE_PATH))))
					.collect(Collectors.toList());

			return collapsedBookmarks.toArray();
		}

	}

}
