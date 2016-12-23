package mesfavoris.internal.views;

import java.util.Optional;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.action.Action;
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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

import com.google.common.collect.Lists;

import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.bookmarktype.IImportTeamProject;
import mesfavoris.commons.core.AdapterUtils;
import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.StatusHelper;
import mesfavoris.internal.actions.AddToRemoteBookmarksStoreAction;
import mesfavoris.internal.actions.CollapseAllAction;
import mesfavoris.internal.actions.ConnectToRemoteBookmarksStoreAction;
import mesfavoris.internal.actions.RefreshRemoteFoldersAction;
import mesfavoris.internal.actions.RemoveFromRemoteBookmarksStoreAction;
import mesfavoris.internal.actions.ToggleLinkAction;
import mesfavoris.internal.jobs.ImportTeamProjectFromBookmarkJob;
import mesfavoris.internal.numberedbookmarks.NumberedBookmarksVirtualFolder;
import mesfavoris.internal.recent.RecentBookmarksVirtualFolder;
import mesfavoris.internal.views.comment.BookmarkCommentArea;
import mesfavoris.internal.visited.LatestVisitedBookmarksVirtualFolder;
import mesfavoris.internal.visited.MostVisitedBookmarksVirtualFolder;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.persistence.IBookmarksDirtyStateTracker;
import mesfavoris.remote.IRemoteBookmarksStore;
import mesfavoris.remote.RemoteBookmarksStoreManager;

public class BookmarksView extends ViewPart {
	private static final String COMMAND_ID_GOTO_FAVORI = "mesfavoris.command.gotoFavori";

	public static final String ID = "mesfavoris.views.BookmarksView";

	private final BookmarkDatabase bookmarkDatabase;
	private final IEventBroker eventBroker;
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;
	private final IBookmarksDirtyStateTracker bookmarksDirtyStateTracker;
	private BookmarksTreeViewer bookmarksTreeViewer;
	private BookmarkCommentArea bookmarkCommentViewer;
	private DrillDownAdapter drillDownAdapter;
	private Action refreshAction;
	private Action collapseAllAction;
	private ToggleLinkAction toggleLinkAction;
	private FormToolkit toolkit;
	private ToolBarManager commentsToolBarManager;
	private IMemento memento;
	private PreviousActivePartListener previousActivePartListener = new PreviousActivePartListener();

	public BookmarksView() {
		this.bookmarkDatabase = BookmarksPlugin.getDefault().getBookmarkDatabase();
		this.eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);
		this.remoteBookmarksStoreManager = BookmarksPlugin.getDefault().getRemoteBookmarksStoreManager();
		this.bookmarksDirtyStateTracker = BookmarksPlugin.getDefault().getBookmarksDirtyStateTracker();
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
		toggleLinkAction.init();
		restoreState(memento);
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
		bookmarkCommentViewer = new BookmarkCommentArea(commentsComposite,
				SWT.MULTI | SWT.V_SCROLL | SWT.WRAP | toolkit.getBorderStyle(), bookmarkDatabase);
		bookmarkCommentViewer.getTextWidget().setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(bookmarkCommentViewer);
		addBulbDecorator(bookmarkCommentViewer.getTextWidget(), "Content assist available");
		bookmarkCommentViewer.setBookmark(null);
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
		Optional<IImportTeamProject> importTeamProject = BookmarksPlugin.getDefault().getImportTeamProjectProvider()
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
		getSite().getPage().removePartListener(previousActivePartListener);
		toggleLinkAction.dispose();
		toolkit.dispose();
		super.dispose();
	}

	private void createTreeControl(Composite parent) {
		IBookmarkPropertiesProvider bookmarkPropertiesProvider = BookmarksPlugin.getDefault()
				.getBookmarkPropertiesProvider();
		BookmarkId rootId = bookmarkDatabase.getBookmarksTree().getRootFolder().getId();
		MostVisitedBookmarksVirtualFolder mostVisitedBookmarksVirtualFolder = new MostVisitedBookmarksVirtualFolder(
				eventBroker, bookmarkDatabase, BookmarksPlugin.getDefault().getMostVisitedBookmarks(), rootId, 10);
		LatestVisitedBookmarksVirtualFolder latestVisitedBookmarksVirtualFolder = new LatestVisitedBookmarksVirtualFolder(
				eventBroker, bookmarkDatabase, BookmarksPlugin.getDefault().getMostVisitedBookmarks(), rootId, 10);
		RecentBookmarksVirtualFolder recentBookmarksVirtualFolder = new RecentBookmarksVirtualFolder(eventBroker,
				bookmarkDatabase, BookmarksPlugin.getDefault().getRecentBookmarks(), rootId, 20);
		NumberedBookmarksVirtualFolder numberedBookmarksVirtualFolder = new NumberedBookmarksVirtualFolder(eventBroker,
				bookmarkDatabase, rootId, BookmarksPlugin.getDefault().getNumberedBookmarks());
		IBookmarksDirtyStateTracker bookmarksDirtyStateTracker = BookmarksPlugin.getDefault()
				.getBookmarksDirtyStateTracker();

		PatternFilter patternFilter = new BookmarkPatternFilter();
		patternFilter.setIncludeLeadingWildcard(true);
		FilteredTree filteredTree = new FilteredTree(parent, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL,
				patternFilter, true) {

			protected TreeViewer doCreateTreeViewer(Composite parent, int style) {
				return new BookmarksTreeViewer(parent, bookmarkDatabase, bookmarksDirtyStateTracker,
						remoteBookmarksStoreManager, bookmarkPropertiesProvider,
						Lists.newArrayList(mostVisitedBookmarksVirtualFolder, latestVisitedBookmarksVirtualFolder,
								recentBookmarksVirtualFolder, numberedBookmarksVirtualFolder));
			};

		};
		filteredTree.setQuickSelectionMode(true);
		bookmarksTreeViewer = (BookmarksTreeViewer) filteredTree.getViewer();
		drillDownAdapter = new DrillDownAdapter(bookmarksTreeViewer);
		bookmarksTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				final Bookmark bookmark = bookmarksTreeViewer.getSelectedBookmark();
				updateCommentsSectionToolbar(bookmark);
				bookmarkCommentViewer.setBookmark(bookmark);
			}
		});
		hookDoubleClickAction();
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
		for (IRemoteBookmarksStore store : BookmarksPlugin.getDefault().getRemoteBookmarksStoreManager()
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
		manager.add(collapseAllAction);
		manager.add(refreshAction);
		manager.add(toggleLinkAction);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		addConnectToRemoteBookmarksStoreActions(manager);
	}

	private void addConnectToRemoteBookmarksStoreActions(IContributionManager manager) {
		for (IRemoteBookmarksStore store : BookmarksPlugin.getDefault().getRemoteBookmarksStoreManager()
				.getRemoteBookmarksStores()) {
			ConnectToRemoteBookmarksStoreAction connectAction = new ConnectToRemoteBookmarksStoreAction(eventBroker,
					store);
			manager.add(connectAction);
		}
	}

	private void makeActions() {
		collapseAllAction = new CollapseAllAction(bookmarksTreeViewer);
		refreshAction = new RefreshRemoteFoldersAction(bookmarkDatabase, remoteBookmarksStoreManager,
				bookmarksDirtyStateTracker);
		toggleLinkAction = new ToggleLinkAction(bookmarkDatabase, getSite(), bookmarksTreeViewer);
	}

	public void setFocus() {
		bookmarksTreeViewer.getControl().setFocus();
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		this.memento = memento;
		super.init(site, memento);
		getSite().getPage().addPartListener(previousActivePartListener);
	}

	@Override
	public void saveState(IMemento memento) {
		BookmarksTreeViewerStateManager manager = new BookmarksTreeViewerStateManager(bookmarksTreeViewer);
		manager.saveState(memento);
	}

	private void restoreState(IMemento memento) {
		BookmarksTreeViewerStateManager manager = new BookmarksTreeViewerStateManager(bookmarksTreeViewer);
		manager.restoreState(memento);
	}

	private void hookDoubleClickAction() {
		bookmarksTreeViewer.addDoubleClickListener(event -> {
			ISelection selection = bookmarksTreeViewer.getSelection();
			Object firstElement = ((IStructuredSelection) selection).getFirstElement();
			Bookmark bookmark = AdapterUtils.getAdapter(firstElement, Bookmark.class);
			if (bookmark instanceof BookmarkFolder) {
				bookmarksTreeViewer.setExpandedState(firstElement, !bookmarksTreeViewer.getExpandedState(firstElement));
			} else {
				IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
				try {
					handlerService.executeCommand(COMMAND_ID_GOTO_FAVORI, null);
				} catch (ExecutionException | NotDefinedException | NotEnabledException | NotHandledException e) {
					StatusHelper.logError("Could not go to bookmark", e);
				}
			}
		});
	}

	public IWorkbenchPart getPreviousActivePart() {
		return previousActivePartListener.getPreviousActivePart();
	}

	private static class PreviousActivePartListener implements IPartListener {
		private IWorkbenchPart previousActivePart;

		@Override
		public void partActivated(IWorkbenchPart part) {
			if (!(part instanceof BookmarksView)) {
				this.previousActivePart = part;
			}
		}

		public IWorkbenchPart getPreviousActivePart() {
			return previousActivePart;
		}

		@Override
		public void partBroughtToTop(IWorkbenchPart part) {
		}

		@Override
		public void partClosed(IWorkbenchPart part) {
			if (previousActivePart == part) {
				previousActivePart = null;
			}
		}

		@Override
		public void partDeactivated(IWorkbenchPart part) {
		}

		@Override
		public void partOpened(IWorkbenchPart part) {
		}

	}

}