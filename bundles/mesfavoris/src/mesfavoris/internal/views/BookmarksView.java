package mesfavoris.internal.views;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

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
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetPage;

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
import mesfavoris.internal.problems.extension.BookmarkProblemHandlers;
import mesfavoris.internal.problems.ui.BookmarkProblemsTooltip;
import mesfavoris.internal.recent.RecentBookmarksVirtualFolder;
import mesfavoris.internal.views.comment.BookmarkCommentArea;
import mesfavoris.internal.visited.LatestVisitedBookmarksVirtualFolder;
import mesfavoris.internal.visited.MostVisitedBookmarksVirtualFolder;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.IBookmarksListener;
import mesfavoris.persistence.IBookmarksDirtyStateTracker;
import mesfavoris.problems.BookmarkProblem;
import mesfavoris.problems.BookmarkProblem.Severity;
import mesfavoris.problems.IBookmarkProblems;
import mesfavoris.remote.IRemoteBookmarksStore;
import mesfavoris.remote.RemoteBookmarksStoreManager;

public class BookmarksView extends ViewPart {
	private static final String COMMAND_ID_GOTO_FAVORI = "mesfavoris.command.gotoFavori";

	public static final String ID = "mesfavoris.views.BookmarksView";

	private final BookmarkDatabase bookmarkDatabase;
	private final IEventBroker eventBroker;
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;
	private final IBookmarksDirtyStateTracker bookmarksDirtyStateTracker;
	private final IBookmarkProblems bookmarkProblems;
	private final BookmarkProblemHandlers bookmarkProblemHandlers;
	private BookmarksTreeViewer bookmarksTreeViewer;
	private BookmarkCommentArea bookmarkCommentViewer;
	private DrillDownAdapter drillDownAdapter;
	private Action refreshAction;
	private Action collapseAllAction;
	private ToggleLinkAction toggleLinkAction;
	private FormToolkit toolkit;
	private Form form;
	private IMemento memento;
	private PreviousActivePartListener previousActivePartListener = new PreviousActivePartListener();
	private Composite commentsComposite;
	private Section commentsSection;
	private BookmarkProblemsTooltip bookmarkProblemsTooltip;
	private Image icon;
	private PropertySheetPage propertyPage;
	private final IBookmarksListener bookmarksListener = (modifications) -> refreshPropertyPage();

	
	public BookmarksView() {
		this.bookmarkDatabase = BookmarksPlugin.getDefault().getBookmarkDatabase();
		this.eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);
		this.remoteBookmarksStoreManager = BookmarksPlugin.getDefault().getRemoteBookmarksStoreManager();
		this.bookmarksDirtyStateTracker = BookmarksPlugin.getDefault().getBookmarksDirtyStateTracker();
		this.bookmarkProblems = BookmarksPlugin.getDefault().getBookmarkProblems();
		this.bookmarkProblemHandlers = BookmarksPlugin.getDefault().getBookmarkProblemHandlers();
	}

	public void createPartControl(Composite parent) {
		GridLayoutFactory.fillDefaults().applyTo(parent);
		toolkit = new FormToolkit(parent.getDisplay());
		toolkit.getHyperlinkGroup().setHyperlinkUnderlineMode(HyperlinkSettings.UNDERLINE_HOVER);
		form = toolkit.createForm(parent);
		icon = BookmarksPlugin.getImageDescriptor("icons/bookmarks-16.png").createImage();
		form.setImage(icon);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(form);
		form.setText("Mes Favoris");
		toolkit.decorateFormHeading(form);
		GridLayoutFactory.swtDefaults().applyTo(form.getBody());
		SashForm sashForm = new SashForm(form.getBody(), SWT.VERTICAL);
		toolkit.adapt(sashForm, true, true);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(sashForm);
		createTreeControl(sashForm);
		createCommentsSection(sashForm);
		makeActions();
		hookContextMenu();
		contributeToActionBars();
		getSite().setSelectionProvider(bookmarksTreeViewer);
		toggleLinkAction.init();
		restoreState(memento);
		bookmarkDatabase.addListener(bookmarksListener);
	}

	private void createCommentsSection(Composite parent) {
		commentsSection = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR);
		commentsSection.setText("Comments");
		commentsSection.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		commentsComposite = toolkit.createComposite(commentsSection);
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

	private void updateFormToolbar(final Bookmark bookmark) {
		IContributionItem[] items = form.getToolBarManager().getItems();

		for (IContributionItem item : items) {
			IContributionItem removed = form.getToolBarManager().remove(item);
			if (removed != null) {
				item.dispose();
			}
		}
		if (bookmark == null) {
			form.getToolBarManager().update(false);
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
			form.getToolBarManager().add(importProjectAction);
		}
		form.getToolBarManager().update(true);
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
		bookmarkDatabase.removeListener(bookmarksListener);
		toggleLinkAction.dispose();
		toolkit.dispose();
		if (icon != null) {
			icon.dispose();
		}
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
				updateFormToolbar(bookmark);
				bookmarkCommentViewer.setBookmark(bookmark);
				updateFormBookmarkProblems(bookmark);
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

	private void updateFormBookmarkProblems(Bookmark bookmark) {
		if (bookmark == null) {
			form.setMessage(null);
			return;
		}
		Set<BookmarkProblem> problems = bookmarkProblems.getBookmarkProblems(bookmark.getId());
		if (problems.size() == 0) {
			form.setMessage(null);
			return;
		}
		int type = problems.iterator().next().getSeverity() == Severity.ERROR ? IMessageProvider.ERROR
				: IMessageProvider.WARNING;
		form.setMessage(problems.size() == 1 ? "One bookmark problem detected"
				: "" + problems.size() + " bookmark problems detected", type);
		if (bookmarkProblemsTooltip == null) {
			Control control = Stream.of(form.getHead().getChildren()).filter(child -> child instanceof CLabel)
					.findFirst().get();
			bookmarkProblemsTooltip = new BookmarkProblemsTooltip(toolkit, control, ToolTip.NO_RECREATE,
					bookmarkProblems, bookmarkProblemHandlers) {
				public Point getLocation(Point tipSize, Event event) {
					Rectangle bounds = control.getBounds();
					return control.getParent().toDisplay(bounds.x, bounds.y + bounds.height);
				}
			};
			bookmarkProblemsTooltip.setHideOnMouseDown(false);
		}
		bookmarkProblemsTooltip.setBookmark(bookmark.getId());
	}

	public IWorkbenchPart getPreviousActivePart() {
		return previousActivePartListener.getPreviousActivePart();
	}

	private void refreshPropertyPage() {
		Display.getDefault().asyncExec(() -> {
		    if (propertyPage != null) {
		        propertyPage.refresh();
		    }
		});
	}

	@Override
	public Object getAdapter(Class adapter) {
	    if (adapter == IPropertySheetPage.class) {
	        if (propertyPage == null) {
	            propertyPage = new PropertySheetPage();
	        }
	        return propertyPage;
	    }
	    return super.getAdapter(adapter);
	}	
	
}