package org.chabanois.mesfavoris.internal.views;

import java.util.function.Predicate;

import org.chabanois.mesfavoris.BookmarksPlugin;
import org.chabanois.mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import org.chabanois.mesfavoris.bookmarktype.IGotoBookmark;
import org.chabanois.mesfavoris.internal.views.dnd.BookmarksViewerDragListener;
import org.chabanois.mesfavoris.internal.views.dnd.BookmarksViewerDropListener;
import org.chabanois.mesfavoris.model.Bookmark;
import org.chabanois.mesfavoris.model.BookmarkDatabase;
import org.chabanois.mesfavoris.model.BookmarkFolder;
import org.chabanois.mesfavoris.model.IBookmarksListener;
import org.chabanois.mesfavoris.remote.AbstractRemoteBookmarksStore;
import org.chabanois.mesfavoris.remote.RemoteBookmarksStoreManager;
import org.chabanois.mesfavoris.validation.BookmarkModificationValidator;
import org.chabanois.mesfavoris.viewers.BookmarksLabelProvider;
import org.chabanois.mesfavoris.viewers.BookmarksLabelProvider.DefaultBookmarkCommentProvider;
import org.chabanois.mesfavoris.viewers.DefaultBookmarkFolderPredicate;
import org.chabanois.mesfavoris.viewers.IBookmarkDecorationProvider;
import org.chabanois.mesfavoris.viewers.RemoteBookmarkFolderDecorationProvider;
import org.chabanois.mesfavoris.viewers.UnderDisconnectedRemoteBookmarkFolderPredicate;
import org.chabanois.mesfavoris.workspace.DefaultBookmarkFolderManager;
import org.chabanois.mesfavoris.workspace.IDefaultBookmarkFolderListener;
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

public class BookmarksTreeViewer extends TreeViewer {
	private static final String SET_AS_DEFAULT_COMMAND_ID = "org.chabanois.mesfavoris.commands.setAsDefaultBookmarkFolder";
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
