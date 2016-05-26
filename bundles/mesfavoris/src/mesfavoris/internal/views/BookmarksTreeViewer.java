package mesfavoris.internal.views;

import java.util.function.Predicate;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.osgi.service.event.EventHandler;

import mesfavoris.BookmarksPlugin;
import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.internal.views.dnd.BookmarksViewerDragListener;
import mesfavoris.internal.views.dnd.BookmarksViewerDropListener;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.IBookmarksListener;
import mesfavoris.remote.AbstractRemoteBookmarksStore;
import mesfavoris.remote.RemoteBookmarksStoreManager;
import mesfavoris.validation.BookmarkModificationValidator;
import mesfavoris.viewers.BookmarksLabelProvider;
import mesfavoris.viewers.DefaultBookmarkFolderPredicate;
import mesfavoris.viewers.IBookmarkDecorationProvider;
import mesfavoris.viewers.RemoteBookmarkFolderDecorationProvider;
import mesfavoris.viewers.UnderDisconnectedRemoteBookmarkFolderPredicate;
import mesfavoris.viewers.BookmarksLabelProvider.DefaultBookmarkCommentProvider;
import mesfavoris.workspace.DefaultBookmarkFolderManager;
import mesfavoris.workspace.IDefaultBookmarkFolderListener;

public class BookmarksTreeViewer extends TreeViewer {
	private static final String SET_AS_DEFAULT_COMMAND_ID = "mesfavoris.commands.setAsDefaultBookmarkFolder";
	private final IEventBroker eventBroker;
	private final BookmarkDatabase bookmarkDatabase;
	private final DefaultBookmarkFolderManager defaultBookmarkFolderManager;
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;
	private final IBookmarkPropertiesProvider bookmarkPropertiesProvider;
	private final IGotoBookmark gotoBookmark;
	private final IBookmarksListener bookmarksListener = (modifications) -> refreshInUIThread();
	private final IDefaultBookmarkFolderListener defaultBookmarkFolderListener = () -> refreshInUIThread();
	private final EventHandler bookmarkStoresEventHandler = (event) -> refresh();

	public BookmarksTreeViewer(Composite parent, BookmarkDatabase bookmarkDatabase,
			DefaultBookmarkFolderManager defaultBookmarkFolderManager,
			RemoteBookmarksStoreManager remoteBookmarksStoreManager,
			IBookmarkPropertiesProvider bookmarkPropertiesProvider, IGotoBookmark gotoBookmark) {
		super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		this.eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);
		this.bookmarkDatabase = bookmarkDatabase;
		this.defaultBookmarkFolderManager = defaultBookmarkFolderManager;
		this.remoteBookmarksStoreManager = remoteBookmarksStoreManager;
		this.bookmarkPropertiesProvider = bookmarkPropertiesProvider;
		this.gotoBookmark = gotoBookmark;
		setContentProvider(new BookmarksTreeContentProvider(bookmarkDatabase));
		BookmarksLabelProvider bookmarksLabelProvider = getBookmarksLabelProvider();
		bookmarksLabelProvider.addListener(event -> refresh());
		setLabelProvider(getBookmarksLabelProvider());
		// viewer.setSorter(new BookmarksViewerSorter());
		setInput(bookmarkDatabase.getBookmarksTree().getRootFolder());
		installDragAndDropSupport();
		hookDoubleClickAction();
		bookmarkDatabase.addListener(bookmarksListener);
		defaultBookmarkFolderManager.addListener(defaultBookmarkFolderListener);
		eventBroker.subscribe(AbstractRemoteBookmarksStore.TOPIC_REMOTE_BOOKMARK_STORES_ALL,
				bookmarkStoresEventHandler);
	}

	private BookmarksLabelProvider getBookmarksLabelProvider() {
		Predicate<Bookmark> selectedBookmarkPredicate = new DefaultBookmarkFolderPredicate(
				defaultBookmarkFolderManager);
		Predicate<Bookmark> disabledBookmarkPredicate = new UnderDisconnectedRemoteBookmarkFolderPredicate(
				bookmarkDatabase, remoteBookmarksStoreManager);
		IBookmarkDecorationProvider bookmarkDecorationProvider = new RemoteBookmarkFolderDecorationProvider(
				remoteBookmarksStoreManager);
		BookmarksLabelProvider bookmarksLabelProvider = new BookmarksLabelProvider(selectedBookmarkPredicate,
				disabledBookmarkPredicate, bookmarkDecorationProvider, BookmarksPlugin.getBookmarkLabelProvider(),
				new DefaultBookmarkCommentProvider());
		return bookmarksLabelProvider;
	}

	@Override
	protected void handleDispose(DisposeEvent event) {
		eventBroker.unsubscribe(bookmarkStoresEventHandler);
		bookmarkDatabase.removeListener(bookmarksListener);
		defaultBookmarkFolderManager.removeListener(defaultBookmarkFolderListener);
		super.handleDispose(event);
	}

	private void hookDoubleClickAction() {
		addDoubleClickListener(event -> {
			ISelection selection = getSelection();
			Bookmark bookmark = (Bookmark) ((IStructuredSelection) selection).getFirstElement();
			if (bookmark instanceof BookmarkFolder) {
				IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench()
						.getService(IHandlerService.class);
				try {
					handlerService.executeCommand(SET_AS_DEFAULT_COMMAND_ID, null);
				} catch (Exception e) {
					// 
				}
			} else {
				IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				gotoBookmark.gotoBookmark(workbenchWindow, bookmark);
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

}
