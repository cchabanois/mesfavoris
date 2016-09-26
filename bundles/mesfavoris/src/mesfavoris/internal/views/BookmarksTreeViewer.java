package mesfavoris.internal.views;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
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

import mesfavoris.BookmarksPlugin;
import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.commons.core.AdapterUtils;
import mesfavoris.internal.jobs.FindLocationAndGotoBookmarkJob;
import mesfavoris.internal.numberedbookmarks.NumberedBookmarkDecorationProvider;
import mesfavoris.internal.views.dnd.BookmarksViewerDragListener;
import mesfavoris.internal.views.dnd.BookmarksViewerDropListener;
import mesfavoris.internal.views.virtual.ExtendedBookmarksTreeContentProvider;
import mesfavoris.internal.views.virtual.VirtualBookmarkFolder;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.IBookmarksListener;
import mesfavoris.persistence.IBookmarksDatabaseDirtyStateListener;
import mesfavoris.persistence.IBookmarksDatabaseDirtyStateTracker;
import mesfavoris.remote.AbstractRemoteBookmarksStore;
import mesfavoris.remote.RemoteBookmarksStoreManager;
import mesfavoris.validation.BookmarkModificationValidator;
import mesfavoris.viewers.BookmarkDecorationProvider;
import mesfavoris.viewers.BookmarksLabelProvider;
import mesfavoris.viewers.BookmarksLabelProvider.DefaultBookmarkCommentProvider;
import mesfavoris.viewers.DirtyBookmarkPredicate;
import mesfavoris.viewers.IBookmarkDecorationProvider;
import mesfavoris.viewers.RemoteBookmarkFolderDecorationProvider;
import mesfavoris.viewers.UnderDisconnectedRemoteBookmarkFolderPredicate;

public class BookmarksTreeViewer extends TreeViewer {
	private final IEventBroker eventBroker;
	private final BookmarkDatabase bookmarkDatabase;
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;
	private final IBookmarkPropertiesProvider bookmarkPropertiesProvider;
	private final IBookmarksListener bookmarksListener = (modifications) -> refreshInUIThread();
	private final IBookmarksDatabaseDirtyStateListener dirtyListener = (dirtyBookmarks) -> refreshInUIThread();
	private final EventHandler bookmarkStoresEventHandler = (event) -> refresh();
	private final DirtyBookmarkPredicate dirtyBookmarkPredicate;
	private final IBookmarksDatabaseDirtyStateTracker bookmarksDatabaseDirtyStateTracker;
	
	public BookmarksTreeViewer(Composite parent, BookmarkDatabase bookmarkDatabase,
			IBookmarksDatabaseDirtyStateTracker bookmarksDatabaseDirtyStateTracker,
			RemoteBookmarksStoreManager remoteBookmarksStoreManager,
			IBookmarkPropertiesProvider bookmarkPropertiesProvider,
			List<VirtualBookmarkFolder> virtualBookmarkFolders) {
		super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		this.eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);
		this.bookmarkDatabase = bookmarkDatabase;
		this.remoteBookmarksStoreManager = remoteBookmarksStoreManager;
		this.bookmarkPropertiesProvider = bookmarkPropertiesProvider;
		this.bookmarksDatabaseDirtyStateTracker = bookmarksDatabaseDirtyStateTracker;
		setContentProvider(new ExtendedBookmarksTreeContentProvider(bookmarkDatabase, virtualBookmarkFolders));
		setUseHashlookup(true);
		this.dirtyBookmarkPredicate = new DirtyBookmarkPredicate(bookmarksDatabaseDirtyStateTracker);
		this.dirtyBookmarkPredicate.init();
		BookmarksLabelProvider bookmarksLabelProvider = getBookmarksLabelProvider();
		bookmarksLabelProvider.addListener(event -> refresh());
		setLabelProvider(getBookmarksLabelProvider());
		setInput(bookmarkDatabase.getBookmarksTree().getRootFolder());
		installDragAndDropSupport();
		hookDoubleClickAction();
		bookmarkDatabase.addListener(bookmarksListener);
		eventBroker.subscribe(AbstractRemoteBookmarksStore.TOPIC_REMOTE_BOOKMARK_STORES_ALL,
				bookmarkStoresEventHandler);
		bookmarksDatabaseDirtyStateTracker.addListener(dirtyListener);
	}
	
	private BookmarksLabelProvider getBookmarksLabelProvider() {
		Predicate<Bookmark> selectedBookmarkPredicate = bookmark -> false;
		Predicate<Bookmark> disabledBookmarkPredicate = new UnderDisconnectedRemoteBookmarkFolderPredicate(
				bookmarkDatabase, remoteBookmarksStoreManager);
		IBookmarkDecorationProvider bookmarkDecorationProvider = new BookmarkDecorationProvider(new RemoteBookmarkFolderDecorationProvider(
				remoteBookmarksStoreManager), new NumberedBookmarkDecorationProvider(BookmarksPlugin.getNumberedBookmarks()));
		BookmarksLabelProvider bookmarksLabelProvider = new BookmarksLabelProvider(selectedBookmarkPredicate,
				disabledBookmarkPredicate, dirtyBookmarkPredicate, bookmarkDecorationProvider,
				BookmarksPlugin.getBookmarkLabelProvider(), new DefaultBookmarkCommentProvider());
		return bookmarksLabelProvider;
	}

	@Override
	protected void handleDispose(DisposeEvent event) {
		bookmarksDatabaseDirtyStateTracker.removeListener(dirtyListener);
		dirtyBookmarkPredicate.dispose();
		eventBroker.unsubscribe(bookmarkStoresEventHandler);
		bookmarkDatabase.removeListener(bookmarksListener);
		super.handleDispose(event);
	}

	private void hookDoubleClickAction() {
		addDoubleClickListener(event -> {
			ISelection selection = getSelection();
			Object firstElement = ((IStructuredSelection) selection).getFirstElement();
			Bookmark bookmark = AdapterUtils.getAdapter(firstElement, Bookmark.class);
			if (bookmark instanceof BookmarkFolder) {
				this.setExpandedState(firstElement, !getExpandedState(firstElement));
			} else {
				new FindLocationAndGotoBookmarkJob(bookmark).schedule();
			}
		});
	}

	private void installDragAndDropSupport() {
		int operations = DND.DROP_MOVE;
		addDragSupport(operations, new Transfer[] { LocalSelectionTransfer.getTransfer() },
				new BookmarksViewerDragListener(this));
		addDropSupport(DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK,
				new Transfer[] { LocalSelectionTransfer.getTransfer(), FileTransfer.getInstance(),
						URLTransfer.getInstance() },
				new BookmarksViewerDropListener(this, bookmarkDatabase,
						new BookmarkModificationValidator(remoteBookmarksStoreManager), bookmarkPropertiesProvider));
	}

	private void refreshInUIThread() {
		Display.getDefault().asyncExec(() -> {
			if (!getControl().isDisposed()) {
				refresh();
			}
		});
	}

	public Bookmark getSelectedBookmark() {
		IStructuredSelection selection = (IStructuredSelection) getSelection();
		if (selection.size() == 0)
			return null;
		return AdapterUtils.getAdapter(selection.getFirstElement(), Bookmark.class);
	}

}
