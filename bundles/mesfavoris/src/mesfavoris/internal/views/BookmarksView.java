package mesfavoris.internal.views;

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
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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
import org.eclipse.ui.IWorkbenchActionConstants;
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
import mesfavoris.bookmarktype.IBookmarkLocationProvider;
import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.bookmarktype.IImportTeamProject;
import mesfavoris.commons.core.AdapterUtils;
import mesfavoris.internal.actions.AddToRemoteBookmarksStoreAction;
import mesfavoris.internal.actions.CollapseAllAction;
import mesfavoris.internal.actions.ConnectToRemoteBookmarksStoreAction;
import mesfavoris.internal.actions.RefreshRemoteFoldersAction;
import mesfavoris.internal.actions.RemoveFromRemoteBookmarksStoreAction;
import mesfavoris.internal.actions.ToggleLinkAction;
import mesfavoris.internal.jobs.ImportTeamProjectFromBookmarkJob;
import mesfavoris.internal.views.comment.BookmarkCommentArea;
import mesfavoris.internal.visited.LatestVisitedBookmarksVirtualFolder;
import mesfavoris.internal.visited.MostVisitedBookmarksVirtualFolder;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.persistence.IBookmarksDatabaseDirtyStateTracker;
import mesfavoris.remote.IRemoteBookmarksStore;
import mesfavoris.remote.RemoteBookmarksStoreManager;
import mesfavoris.validation.BookmarkModificationValidator;
import mesfavoris.validation.IBookmarkModificationValidator;
import mesfavoris.workspace.DefaultBookmarkFolderManager;

public class BookmarksView extends ViewPart {
	public static final String ID = "mesfavoris.views.BookmarksView";

	private final BookmarkDatabase bookmarkDatabase;
	private final IEventBroker eventBroker;
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;
	private final IBookmarksDatabaseDirtyStateTracker bookmarksDatabaseDirtyStateTracker;
	private final IBookmarkLocationProvider bookmarkLocationProvider;
	private final IGotoBookmark gotoBookmark;
	private Text searchText;
	private BookmarksTreeViewer bookmarksTreeViewer;
	private BookmarkCommentArea bookmarkCommentViewer;
	private DrillDownAdapter drillDownAdapter;
	private Action refreshAction;
	private Action collapseAllAction;
	private ToggleLinkAction toggleLinkAction;
	private FormToolkit toolkit;
	private ToolBarManager commentsToolBarManager;

	public BookmarksView() {
		this.bookmarkDatabase = BookmarksPlugin.getBookmarkDatabase();
		this.eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);
		this.remoteBookmarksStoreManager = BookmarksPlugin.getRemoteBookmarksStoreManager();
		this.bookmarksDatabaseDirtyStateTracker = BookmarksPlugin.getBookmarksDatabaseDirtyStateTracker();
		this.bookmarkLocationProvider = BookmarksPlugin.getBookmarkLocationProvider();
		this.gotoBookmark = BookmarksPlugin.getGotoBookmark();
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
		bookmarkCommentViewer = new BookmarkCommentArea(commentsComposite,
				SWT.MULTI | SWT.V_SCROLL | SWT.WRAP | toolkit.getBorderStyle(), bookmarkDatabase,
				bookmarkModificationValidator);
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
		toggleLinkAction.dispose();
		toolkit.dispose();
		super.dispose();
	}

	private void createTreeControl(Composite parent) {
		// GridLayout gridLayout = new GridLayout(1, false);
		// parent.setLayout(gridLayout);
		// searchText = new Text(parent, SWT.ICON_SEARCH);
		// searchText.setLayoutData(new GridData(style));
		DefaultBookmarkFolderManager defaultBookmarkFolderManager = BookmarksPlugin.getDefaultBookmarkFolderManager();
		IBookmarkPropertiesProvider bookmarkPropertiesProvider = BookmarksPlugin.getBookmarkPropertiesProvider();
		MostVisitedBookmarksVirtualFolder mostVisitedBookmarksVirtualFolder = new MostVisitedBookmarksVirtualFolder(
				eventBroker, bookmarkDatabase, BookmarksPlugin.getMostVisitedBookmarks(),
				bookmarkDatabase.getBookmarksTree().getRootFolder().getId(), 10);
		LatestVisitedBookmarksVirtualFolder latestVisitedBookmarksVirtualFolder = new LatestVisitedBookmarksVirtualFolder(
				eventBroker, bookmarkDatabase, BookmarksPlugin.getMostVisitedBookmarks(),
				bookmarkDatabase.getBookmarksTree().getRootFolder().getId(), 10);
		bookmarksTreeViewer = new BookmarksTreeViewer(parent, bookmarkDatabase, defaultBookmarkFolderManager,
				remoteBookmarksStoreManager, bookmarkPropertiesProvider, bookmarkLocationProvider, gotoBookmark,
				Lists.newArrayList(mostVisitedBookmarksVirtualFolder, latestVisitedBookmarksVirtualFolder));
		drillDownAdapter = new DrillDownAdapter(bookmarksTreeViewer);
		bookmarksTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				final Bookmark bookmark = bookmarksTreeViewer.getSelectedBookmark();
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
		manager.add(collapseAllAction);
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
		collapseAllAction = new CollapseAllAction(bookmarksTreeViewer);
		refreshAction = new RefreshRemoteFoldersAction(bookmarkDatabase, remoteBookmarksStoreManager,
				bookmarksDatabaseDirtyStateTracker);
		toggleLinkAction = new ToggleLinkAction(bookmarkDatabase, getSite(), bookmarksTreeViewer);
	}

	public void setFocus() {
		bookmarksTreeViewer.getControl().setFocus();
	}

}