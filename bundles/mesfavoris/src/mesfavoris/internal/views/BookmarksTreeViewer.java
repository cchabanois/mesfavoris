package mesfavoris.internal.views;

import java.util.List;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.URLTransfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.event.EventHandler;

import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.views.dnd.BookmarksViewerDragListener;
import mesfavoris.internal.views.dnd.BookmarksViewerDropListener;
import mesfavoris.internal.views.virtual.ExtendedBookmarksTreeContentProvider;
import mesfavoris.internal.views.virtual.VirtualBookmarkFolder;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.IBookmarksListener;
import mesfavoris.persistence.IBookmarksDirtyStateListener;
import mesfavoris.persistence.IBookmarksDirtyStateTracker;
import mesfavoris.remote.AbstractRemoteBookmarksStore;
import mesfavoris.remote.RemoteBookmarksStoreManager;
import mesfavoris.topics.BookmarksEvents;
import mesfavoris.ui.viewers.BookmarksLabelProvider;

public class BookmarksTreeViewer extends TreeViewer {
	private static final int REFRESH_DELAY = 500;
	private final IEventBroker eventBroker;
	private final BookmarkDatabase bookmarkDatabase;
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;
	private final IBookmarkPropertiesProvider bookmarkPropertiesProvider;
	private final IBookmarksListener bookmarksListener = (modifications) -> scheduleRefresh();
	private final IBookmarksDirtyStateListener dirtyListener = (dirtyBookmarks) -> scheduleRefresh();
	private final EventHandler bookmarkStoresEventHandler = (event) -> refresh();
	private final EventHandler bookmarkProblemsEventHandler = (event) -> refresh();
	private final IBookmarksDirtyStateTracker bookmarksDirtyStateTracker;
	private final Job refreshInUIThreadJob = new RefreshInUIThreadJob();
	
	public BookmarksTreeViewer(Composite parent, BookmarkDatabase bookmarkDatabase,
			IBookmarksDirtyStateTracker bookmarksDirtyStateTracker,
			RemoteBookmarksStoreManager remoteBookmarksStoreManager,
			IBookmarkPropertiesProvider bookmarkPropertiesProvider,
			List<VirtualBookmarkFolder> virtualBookmarkFolders) {
		super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		this.eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);
		this.bookmarkDatabase = bookmarkDatabase;
		this.remoteBookmarksStoreManager = remoteBookmarksStoreManager;
		this.bookmarkPropertiesProvider = bookmarkPropertiesProvider;
		this.bookmarksDirtyStateTracker = bookmarksDirtyStateTracker;
		setContentProvider(new ExtendedBookmarksTreeContentProvider(bookmarkDatabase, virtualBookmarkFolders));
		setUseHashlookup(true);
		BookmarksLabelProvider bookmarksLabelProvider = getBookmarksLabelProvider();
		bookmarksLabelProvider.addListener(event -> refresh());
		setLabelProvider(bookmarksLabelProvider);
		setInput(bookmarkDatabase.getBookmarksTree().getRootFolder());
		installDragAndDropSupport();
		bookmarkDatabase.addListener(bookmarksListener);
		eventBroker.subscribe(AbstractRemoteBookmarksStore.TOPIC_REMOTE_BOOKMARK_STORES_ALL,
				bookmarkStoresEventHandler);
		eventBroker.subscribe(BookmarksEvents.TOPIC_BOOKMARK_PROBLEMS_CHANGED, bookmarkProblemsEventHandler);
		bookmarksDirtyStateTracker.addListener(dirtyListener);
	}

	private BookmarksLabelProvider getBookmarksLabelProvider() {
		BookmarksLabelProvider bookmarksLabelProvider = new BookmarksLabelProvider(bookmarkDatabase,
				remoteBookmarksStoreManager, bookmarksDirtyStateTracker,
				BookmarksPlugin.getDefault().getBookmarkLabelProvider(),
				BookmarksPlugin.getDefault().getNumberedBookmarks(),
				BookmarksPlugin.getDefault().getBookmarkProblems());
		return bookmarksLabelProvider;
	}

	@Override
	protected void handleDispose(DisposeEvent event) {
		bookmarksDirtyStateTracker.removeListener(dirtyListener);
		eventBroker.unsubscribe(bookmarkStoresEventHandler);
		eventBroker.unsubscribe(bookmarkProblemsEventHandler);
		bookmarkDatabase.removeListener(bookmarksListener);
		super.handleDispose(event);
	}

	private void installDragAndDropSupport() {
		int operations = DND.DROP_MOVE;
		addDragSupport(operations, new Transfer[] { LocalSelectionTransfer.getTransfer() },
				new BookmarksViewerDragListener(this));
		addDropSupport(DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK,
				new Transfer[] { LocalSelectionTransfer.getTransfer(), FileTransfer.getInstance(),
						URLTransfer.getInstance() },
				new BookmarksViewerDropListener(this, bookmarkDatabase, bookmarkPropertiesProvider));
	}

	private void refreshInUIThread() {
		Display.getDefault().asyncExec(() -> {
			if (!getControl().isDisposed()) {
				refresh();
			}
		});
	}
	
	private void scheduleRefresh() {
		refreshInUIThreadJob.schedule(REFRESH_DELAY);
	}

	public Bookmark getSelectedBookmark() {
		IStructuredSelection selection = (IStructuredSelection) getSelection();
		if (selection.size() == 0)
			return null;
		return Adapters.adapt(selection.getFirstElement(), Bookmark.class);
	}

	public BookmarkDatabase getBookmarkDatabase() {
		return bookmarkDatabase;
	}

	/*
	 * Job used to refresh the tree viewer. We don't want to refresh it on every key press when the user edit the comment
	 */
	private class RefreshInUIThreadJob extends Job {

		public RefreshInUIThreadJob() {
			super("Refresh Bookmarks tree");
			setPriority(INTERACTIVE);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (!getControl().isDisposed()) {
				refreshInUIThread();
			}
			return Status.OK_STATUS;
		}
		
	}
	
}
