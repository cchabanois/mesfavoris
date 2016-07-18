package mesfavoris.internal.views;

import java.util.List;
import java.util.Optional;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

import com.google.common.collect.Lists;

import mesfavoris.BookmarksPlugin;
import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.bookmarktype.IImportTeamProject;
import mesfavoris.commons.core.AdapterUtils;
import mesfavoris.internal.actions.AddToRemoteBookmarksStoreAction;
import mesfavoris.internal.actions.ConnectToRemoteBookmarksStoreAction;
import mesfavoris.internal.actions.RemoveFromRemoteBookmarksStoreAction;
import mesfavoris.internal.jobs.ImportTeamProjectFromBookmarkJob;
import mesfavoris.internal.operations.GetLinkedBookmarksOperation;
import mesfavoris.internal.visited.LatestVisitedBookmarksVirtualFolder;
import mesfavoris.internal.visited.MostVisitedBookmarksVirtualFolder;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.remote.IRemoteBookmarksStore;
import mesfavoris.remote.RemoteBookmarksStoreManager;
import mesfavoris.validation.BookmarkModificationValidator;
import mesfavoris.validation.IBookmarkModificationValidator;
import mesfavoris.workspace.DefaultBookmarkFolderManager;

public class BookmarksView extends ViewPart {
	public static final String ID = "mesfavoris.views.BookmarksView";

	private final BookmarkDatabase bookmarkDatabase;
	private final IEventBroker eventBroker;
	private Text searchText;
	private BookmarksTreeViewer bookmarksTreeViewer;
	private BookmarkCommentViewer bookmarkCommentViewer;
	private DrillDownAdapter drillDownAdapter;
	private Action refreshAction;
	private FormToolkit toolkit;
	private ToolBarManager commentsToolBarManager;

	public BookmarksView() {
		this.bookmarkDatabase = BookmarksPlugin.getBookmarkDatabase();
		this.eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);
	}

	public void createPartControl(Composite parent) {
		GridLayoutFactory.fillDefaults().applyTo(parent);
		toolkit = new FormToolkit(parent.getDisplay());
		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		toolkit.adapt(sashForm, true, true);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(sashForm);
		createTreeControl(sashForm);
		createCommentsSection(sashForm);

		sashForm.setWeights(new int[] { 70, 30 });

		makeActions();
		hookContextMenu();
		contributeToActionBars();
		getSite().setSelectionProvider(bookmarksTreeViewer);
		getSite().getWorkbenchWindow().getPartService().addPartListener(partListener);
	}

	private void createCommentsSection(Composite parent) {
		Section commentsSection = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR);
		commentsSection.setText("Comments");
		commentsSection.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		createCommentsSectionToolbar(commentsSection);

		Composite commentsComposite = toolkit.createComposite(commentsSection);
		toolkit.paintBordersFor(commentsComposite);
		commentsSection.setClient(commentsComposite);
		GridLayoutFactory.fillDefaults().extendedMargins(2, 2, 2, 2).applyTo(commentsComposite);
		IBookmarkModificationValidator bookmarkModificationValidator = new BookmarkModificationValidator(
				BookmarksPlugin.getRemoteBookmarksStoreManager());
		bookmarkCommentViewer = new BookmarkCommentViewer(commentsComposite,
				SWT.MULTI | SWT.V_SCROLL | SWT.WRAP | toolkit.getBorderStyle(), bookmarkDatabase,
				bookmarkModificationValidator);
		bookmarkCommentViewer.getControl().setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(bookmarkCommentViewer.getControl());
		addBulbDecorator(bookmarkCommentViewer.getControl(), "Content assist available");
		TextSourceViewerConfiguration configuration = new TextSourceViewerConfiguration(EditorsUI.getPreferenceStore());
		bookmarkCommentViewer.configure(configuration);
		bookmarkCommentViewer.setBookmark(null);
	}

	private Bookmark getSelectedBookmark() {
		IStructuredSelection selection = (IStructuredSelection) bookmarksTreeViewer.getSelection();
		if (selection.size() == 0)
			return null;
		return AdapterUtils.getAdapter(selection.getFirstElement(), Bookmark.class);
	}

	private void createCommentsSectionToolbar(Section commentsSection) {
		this.commentsToolBarManager = new ToolBarManager(SWT.FLAT | SWT.HORIZONTAL);
		ToolBar toolbar = commentsToolBarManager.createControl(commentsSection);
		commentsSection.setTextClient(toolbar);
	}

	private void updateCommentsSectionToolbar(final Bookmark bookmark) {
		IContributionItem[] items = commentsToolBarManager.getItems();

		for (IContributionItem item : items) {
			IContributionItem removed = commentsToolBarManager.remove(item);
			if (removed != null) {
				item.dispose();
			}
		}
		if (bookmark == null) {
			commentsToolBarManager.update(false);
			return;
		}
		Optional<IImportTeamProject> importTeamProject = BookmarksPlugin.getImportTeamProjectProvider()
				.getHandler(bookmark);
		if (importTeamProject.isPresent()) {
			Action importProjectAction = new Action("Import project", IAction.AS_PUSH_BUTTON) {

				public void run() {
					ImportTeamProjectFromBookmarkJob job = new ImportTeamProjectFromBookmarkJob(bookmark);
					job.schedule();
				}
			};
			importProjectAction.setImageDescriptor(importTeamProject.get().getIcon());
			commentsToolBarManager.add(importProjectAction);
		}
		commentsToolBarManager.add(new Action("Update", IAction.AS_PUSH_BUTTON) {

			public void run() {

			}
		});
		commentsToolBarManager.update(true);
	}

	private void addBulbDecorator(final Control control, final String tooltip) {
		ControlDecoration dec = new ControlDecoration(control, SWT.TOP | SWT.LEFT);

		dec.setImage(FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL).getImage());

		dec.setShowOnlyOnFocus(true);
		dec.setShowHover(true);

		dec.setDescriptionText(tooltip);
	}

	@Override
	public void dispose() {
		getSite().getWorkbenchWindow().getPartService().removePartListener(partListener);
		toolkit.dispose();
		super.dispose();
	}

	private void createTreeControl(Composite parent) {
		// GridLayout gridLayout = new GridLayout(1, false);
		// parent.setLayout(gridLayout);
		// searchText = new Text(parent, SWT.ICON_SEARCH);
		// searchText.setLayoutData(new GridData(style));
		DefaultBookmarkFolderManager defaultBookmarkFolderManager = BookmarksPlugin.getDefaultBookmarkFolderManager();
		RemoteBookmarksStoreManager remoteBookmarksStoreManager = BookmarksPlugin.getRemoteBookmarksStoreManager();
		IBookmarkPropertiesProvider bookmarkPropertiesProvider = BookmarksPlugin.getBookmarkPropertiesProvider();
		MostVisitedBookmarksVirtualFolder mostVisitedBookmarksVirtualFolder = new MostVisitedBookmarksVirtualFolder(
				eventBroker,
				bookmarkDatabase, BookmarksPlugin.getMostVisitedBookmarks(),
				bookmarkDatabase.getBookmarksTree().getRootFolder().getId(), 10);
		LatestVisitedBookmarksVirtualFolder latestVisitedBookmarksVirtualFolder = new LatestVisitedBookmarksVirtualFolder(
				eventBroker,
				bookmarkDatabase, BookmarksPlugin.getMostVisitedBookmarks(),
				bookmarkDatabase.getBookmarksTree().getRootFolder().getId(), 10);
		IGotoBookmark gotoBookmark = BookmarksPlugin.getGotoBookmark();
		bookmarksTreeViewer = new BookmarksTreeViewer(parent, bookmarkDatabase, defaultBookmarkFolderManager,
				remoteBookmarksStoreManager, bookmarkPropertiesProvider, gotoBookmark,
				Lists.newArrayList(mostVisitedBookmarksVirtualFolder, latestVisitedBookmarksVirtualFolder));
		drillDownAdapter = new DrillDownAdapter(bookmarksTreeViewer);
		bookmarksTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				final Bookmark bookmark = getSelectedBookmark();
				updateCommentsSectionToolbar(bookmark);
				bookmarkCommentViewer.setBookmark(bookmark);
			}
		});
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				BookmarksView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(bookmarksTreeViewer.getControl());
		bookmarksTreeViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, bookmarksTreeViewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(refreshAction);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillContextMenu(IMenuManager manager) {
		addRemoteBookmarksStoreActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void addRemoteBookmarksStoreActions(IMenuManager manager) {
		for (IRemoteBookmarksStore store : BookmarksPlugin.getRemoteBookmarksStoreManager()
				.getRemoteBookmarksStores()) {
			MenuManager subMenu = new MenuManager(store.getDescriptor().getLabel(),
					ID + "." + store.getDescriptor().getId());
			AddToRemoteBookmarksStoreAction addToStoreAction = new AddToRemoteBookmarksStoreAction(eventBroker,
					bookmarksTreeViewer, store);
			RemoveFromRemoteBookmarksStoreAction removeFromStoreAction = new RemoveFromRemoteBookmarksStoreAction(
					eventBroker, bookmarksTreeViewer, store);
			subMenu.add(addToStoreAction);
			subMenu.add(removeFromStoreAction);
			// Other plug-ins can contribute there actions here
			subMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
			manager.add(subMenu);
		}
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		ControlContribution controlContribution = createSearchTextControlContribution();
		manager.add(controlContribution);
		manager.add(refreshAction);
		manager.add(toggleLinkAction);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		addConnectToRemoteBookmarksStoreActions(manager);
	}

	private void addConnectToRemoteBookmarksStoreActions(IContributionManager manager) {
		for (IRemoteBookmarksStore store : BookmarksPlugin.getRemoteBookmarksStoreManager()
				.getRemoteBookmarksStores()) {
			ConnectToRemoteBookmarksStoreAction connectAction = new ConnectToRemoteBookmarksStoreAction(eventBroker,
					store);
			manager.add(connectAction);
		}
	}

	private ControlContribution createSearchTextControlContribution() {
		ControlContribution controlContribution = new ControlContribution("StagingView.searchText") {
			@Override
			protected Control createControl(Composite parent) {
				Composite toolbarComposite = toolkit.createComposite(parent, SWT.NONE);
				toolbarComposite.setBackground(null);
				GridLayout headLayout = new GridLayout();
				headLayout.numColumns = 2;
				headLayout.marginHeight = 0;
				headLayout.marginWidth = 0;
				headLayout.marginTop = 0;
				headLayout.marginBottom = 0;
				headLayout.marginLeft = 0;
				headLayout.marginRight = 0;
				toolbarComposite.setLayout(headLayout);

				Text filterText = new Text(toolbarComposite, SWT.SEARCH | SWT.ICON_CANCEL | SWT.ICON_SEARCH);
				filterText.setMessage("Filter bookmarks");
				GridData data = new GridData(GridData.FILL_HORIZONTAL);
				data.widthHint = 150;
				filterText.setLayoutData(data);
				final Display display = Display.getCurrent();
				filterText.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						// final StagingViewSearchThread searchThread = new
						// StagingViewSearchThread(
						// StagingView.this);
						// display.timerExec(200, new Runnable() {
						// public void run() {
						// searchThread.start();
						// }
						// }
						// );
					}
				});
				return toolbarComposite;
			}
		};
		return controlContribution;
	}

	private void makeActions() {
		refreshAction = new Action() {
			public void run() {
				refreshInUIThread();
			}
		};
		refreshAction.setText("&Refresh");
		refreshAction.setToolTipText("Refresh bookmarks");
		refreshAction.setImageDescriptor(
				BookmarksPlugin.imageDescriptorFromPlugin(BookmarksPlugin.PLUGIN_ID, "icons/refresh.gif"));

		toggleLinkAction = new ToggleLinkAction();
		toggleLinkAction.setActionDefinitionId(IWorkbenchCommandConstants.NAVIGATE_TOGGLE_LINK_WITH_EDITOR);
		toggleLinkAction.updateLinkImage(false);
	}

	public void setFocus() {
		bookmarksTreeViewer.getControl().setFocus();
	}

	private void refreshInUIThread() {
		Display.getDefault().asyncExec(() -> {
			if (bookmarksTreeViewer != null && !bookmarksTreeViewer.getControl().isDisposed()) {
				bookmarksTreeViewer.refresh();
			}
		});
	}

	private ToggleLinkAction toggleLinkAction;

	private boolean linking = true;

	private IWorkbenchPart lastSelectionProviderPart;

	private void setLinkingEnabled(boolean enabled) {
		linking = enabled;

		if (linking && lastSelectionProviderPart != null) {
			selectBookmarkFromLinkedPart(lastSelectionProviderPart);
		}
	}

	/**
	 * Start to listen for selection changes.
	 */
	private void startListeningForSelectionChanges() {
		getSite().getPage().addPostSelectionListener(selectionListener);
	}

	/**
	 * Stop to listen for selection changes.
	 */
	private void stopListeningForSelectionChanges() {
		getSite().getPage().removePostSelectionListener(selectionListener);
	}

	private class ToggleLinkAction extends Action {
		private static final String SYNCED_GIF = "synced.gif";
		private static final String SYNC_BROKEN_GIF = "sync_broken.gif";

		private String fIconName;

		public ToggleLinkAction() {
			super("Link with Selection", SWT.TOGGLE);
			setToolTipText("Link with Selection");
			setChecked(linking);
		}

		@Override
		public void run() {
			setLinkingEnabled(!linking);
			updateLinkImage(false);
		}

		private void updateLinkImage(boolean isBroken) {
			String iconName = isBroken ? SYNC_BROKEN_GIF : SYNCED_GIF;
			if (!iconName.equals(fIconName)) {
				ImageDescriptor id = BookmarksPlugin.getImageDescriptor("icons/dlcl16/" + iconName);
				if (id != null)
					this.setDisabledImageDescriptor(id);

				ImageDescriptor descriptor = BookmarksPlugin.getImageDescriptor("icons/elcl16/" + iconName);
				this.setHoverImageDescriptor(descriptor);
				this.setImageDescriptor(descriptor);

				setToolTipText(isBroken ? "Link with Selection (showing last valid input)" : "Link with Selection");
				fIconName = iconName;
			}
		}

	}

	private ISelectionListener selectionListener = new ISelectionListener() {

		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (part.equals(this))
				return;

			lastSelectionProviderPart = part;

			if (linking) {
				selectBookmarkFromLinkedPart(part);
			}
		}
	};

	/**
	 * Select the bookmark corresponding to the selection in given
	 * {@link IWorkbenchPart}
	 * 
	 * @param part
	 */
	private void selectBookmarkFromLinkedPart(IWorkbenchPart part) {
		ISelectionProvider provider = part.getSite().getSelectionProvider();
		if (provider == null) {
			toggleLinkAction.updateLinkImage(true);
			return;
		}

		ISelection selection = provider.getSelection();
		if (selection == null) {
			toggleLinkAction.updateLinkImage(true);
			return;
		}
		GetLinkedBookmarksOperation getLinkedBookmarksOperation = new GetLinkedBookmarksOperation(bookmarkDatabase);
		List<Bookmark> bookmarks = getLinkedBookmarksOperation.getLinkedBookmarks(part, selection);
		if (bookmarks.isEmpty()) {
			toggleLinkAction.updateLinkImage(true);
			return;
		}
		toggleLinkAction.updateLinkImage(false);
		bookmarksTreeViewer.setSelection(new StructuredSelection(bookmarks.get(0)), true);
	}

	private IPartListener2 partListener = new IPartListener2() {
		public void partVisible(IWorkbenchPartReference ref) {
			if (ref.getId().equals(getSite().getId())) {
				IWorkbenchPart activePart = ref.getPage().getActivePart();
				if (activePart != null)
					selectionListener.selectionChanged(activePart, ref.getPage().getSelection());
				startListeningForSelectionChanges();
			}
		}

		public void partHidden(IWorkbenchPartReference ref) {
			if (ref.getId().equals(getSite().getId()))
				stopListeningForSelectionChanges();
		}

		public void partInputChanged(IWorkbenchPartReference ref) {
			if (!ref.getId().equals(getSite().getId())) {
				selectBookmarkFromLinkedPart(ref.getPart(false));
			}
		}

		public void partActivated(IWorkbenchPartReference ref) {
		}

		public void partBroughtToTop(IWorkbenchPartReference ref) {
		}

		public void partClosed(IWorkbenchPartReference ref) {
		}

		public void partDeactivated(IWorkbenchPartReference ref) {
		}

		public void partOpened(IWorkbenchPartReference ref) {
		}
	};

}