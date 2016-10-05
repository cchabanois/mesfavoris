package mesfavoris.internal.service;

import java.util.List;
import java.util.function.Consumer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;

import mesfavoris.BookmarksException;
import mesfavoris.bookmarktype.IBookmarkLocationProvider;
import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.internal.numberedbookmarks.BookmarkNumber;
import mesfavoris.internal.numberedbookmarks.NumberedBookmarks;
import mesfavoris.internal.service.operations.AddBookmarkFolderOperation;
import mesfavoris.internal.service.operations.AddBookmarkOperation;
import mesfavoris.internal.service.operations.AddBookmarksTreeOperation;
import mesfavoris.internal.service.operations.AddNumberedBookmarkOperation;
import mesfavoris.internal.service.operations.AddToRemoteBookmarksStoreOperation;
import mesfavoris.internal.service.operations.ConnectToRemoteBookmarksStoreOperation;
import mesfavoris.internal.service.operations.CopyBookmarkOperation;
import mesfavoris.internal.service.operations.CutBookmarkOperation;
import mesfavoris.internal.service.operations.DeleteBookmarksOperation;
import mesfavoris.internal.service.operations.GetLinkedBookmarksOperation;
import mesfavoris.internal.service.operations.GotoBookmarkOperation;
import mesfavoris.internal.service.operations.GotoNumberedBookmarkOperation;
import mesfavoris.internal.service.operations.PasteBookmarkOperation;
import mesfavoris.internal.service.operations.RefreshRemoteFolderOperation;
import mesfavoris.internal.service.operations.RemoveFromRemoteBookmarksStoreOperation;
import mesfavoris.internal.service.operations.RenameBookmarkOperation;
import mesfavoris.internal.service.operations.SetBookmarkCommentOperation;
import mesfavoris.internal.service.operations.ShowInBookmarksViewOperation;
import mesfavoris.internal.service.operations.SortByNameOperation;
import mesfavoris.internal.service.operations.UpdateBookmarkOperation;
import mesfavoris.internal.workspace.DefaultBookmarkFolderProvider;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.persistence.IBookmarksDirtyStateTracker;
import mesfavoris.remote.RemoteBookmarksStoreManager;
import mesfavoris.service.IBookmarksService;
import mesfavoris.validation.IBookmarkModificationValidator;

public class BookmarksService implements IBookmarksService {
	private final BookmarkDatabase bookmarkDatabase;
	private final IBookmarkModificationValidator bookmarkModificationValidator;
	private final IBookmarkPropertiesProvider bookmarkPropertiesProvider;
	private final DefaultBookmarkFolderProvider defaultBookmarkFolderProvider;
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;
	private final IBookmarksDirtyStateTracker bookmarksDirtyStateTracker;
	private final IBookmarkLocationProvider bookmarkLocationProvider;
	private final IGotoBookmark gotoBookmark;
	private final NumberedBookmarks numberedBookmarks;

	public BookmarksService(BookmarkDatabase bookmarkDatabase,
			IBookmarkModificationValidator bookmarkModificationValidator,
			IBookmarkPropertiesProvider bookmarkPropertiesProvider,
			DefaultBookmarkFolderProvider defaultBookmarkFolderProvider,
			RemoteBookmarksStoreManager remoteBookmarksStoreManager,
			IBookmarksDirtyStateTracker bookmarksDirtyStateTracker,
			IBookmarkLocationProvider bookmarkLocationProvider, IGotoBookmark gotoBookmark,
			NumberedBookmarks numberedBookmarks) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.bookmarkModificationValidator = bookmarkModificationValidator;
		this.bookmarkPropertiesProvider = bookmarkPropertiesProvider;
		this.defaultBookmarkFolderProvider = defaultBookmarkFolderProvider;
		this.remoteBookmarksStoreManager = remoteBookmarksStoreManager;
		this.bookmarksDirtyStateTracker = bookmarksDirtyStateTracker;
		this.bookmarkLocationProvider = bookmarkLocationProvider;
		this.gotoBookmark = gotoBookmark;
		this.numberedBookmarks = numberedBookmarks;
	}

	@Override
	public BookmarksTree getBookmarksTree() {
		return bookmarkDatabase.getBookmarksTree();
	}

	@Override
	public void addBookmarkFolder(BookmarkId parentFolderId, String folderName) throws BookmarksException {
		AddBookmarkFolderOperation operation = new AddBookmarkFolderOperation(bookmarkDatabase,
				bookmarkModificationValidator);
		operation.addBookmarkFolder(parentFolderId, folderName);
	}

	@Override
	public BookmarkId addBookmark(IWorkbenchPart part, ISelection selection, IProgressMonitor monitor)
			throws BookmarksException {
		AddBookmarkOperation operation = new AddBookmarkOperation(bookmarkDatabase, bookmarkPropertiesProvider,
				defaultBookmarkFolderProvider);
		return operation.addBookmark(part, selection, monitor);
	}

	@Override
	public void addBookmarksTree(BookmarkId parentBookmarkId, BookmarksTree sourceBookmarksTree,
			Consumer<BookmarksTree> afterCommit) throws BookmarksException {
		AddBookmarksTreeOperation operation = new AddBookmarksTreeOperation(bookmarkDatabase,
				bookmarkModificationValidator);
		operation.addBookmarksTree(parentBookmarkId, sourceBookmarksTree, afterCommit);
	}

	@Override
	public void addToRemoteBookmarksStore(String storeId, final BookmarkId bookmarkFolderId,
			final IProgressMonitor monitor) throws BookmarksException {
		AddToRemoteBookmarksStoreOperation operation = new AddToRemoteBookmarksStoreOperation(bookmarkDatabase,
				remoteBookmarksStoreManager);
		operation.addToRemoteBookmarksStore(storeId, bookmarkFolderId, monitor);
	}

	@Override
	public void connectToRemoteBookmarksStore(String storeId, IProgressMonitor monitor) throws BookmarksException {
		ConnectToRemoteBookmarksStoreOperation operation = new ConnectToRemoteBookmarksStoreOperation(bookmarkDatabase,
				remoteBookmarksStoreManager, bookmarksDirtyStateTracker);
		operation.connectToRemoteBookmarksStore(storeId, monitor);
	}

	@Override
	public void copyToClipboard(List<BookmarkId> selection) {
		CopyBookmarkOperation operation = new CopyBookmarkOperation();
		operation.copyToClipboard(bookmarkDatabase.getBookmarksTree(), selection);
	}

	@Override
	public void cutToClipboard(List<BookmarkId> selection) throws BookmarksException {
		CutBookmarkOperation operation = new CutBookmarkOperation(bookmarkDatabase, bookmarkModificationValidator);
		operation.cutToClipboard(selection);
	}

	@Override
	public void deleteBookmarks(final List<BookmarkId> selection) throws BookmarksException {
		DeleteBookmarksOperation operation = new DeleteBookmarksOperation(bookmarkDatabase,
				bookmarkModificationValidator);
		operation.deleteBookmarks(selection);
	}

	@Override
	public List<Bookmark> getLinkedBookmarks(IWorkbenchPart part, ISelection selection) {
		GetLinkedBookmarksOperation operation = new GetLinkedBookmarksOperation(bookmarkDatabase);
		return operation.getLinkedBookmarks(part, selection);
	}

	@Override
	public void paste(Display display, BookmarkId parentBookmarkId, IProgressMonitor monitor)
			throws BookmarksException {
		PasteBookmarkOperation operation = new PasteBookmarkOperation(bookmarkDatabase, bookmarkPropertiesProvider,
				bookmarkModificationValidator);
		operation.paste(display, parentBookmarkId, monitor);
	}

	@Override
	public void refresh(BookmarkId bookmarkFolderId, IProgressMonitor monitor) throws BookmarksException {
		RefreshRemoteFolderOperation operation = new RefreshRemoteFolderOperation(bookmarkDatabase,
				remoteBookmarksStoreManager, bookmarksDirtyStateTracker);
		operation.refresh(bookmarkFolderId, monitor);
	}

	@Override
	public void refresh(IProgressMonitor monitor) throws BookmarksException {
		RefreshRemoteFolderOperation operation = new RefreshRemoteFolderOperation(bookmarkDatabase,
				remoteBookmarksStoreManager, bookmarksDirtyStateTracker);
		operation.refresh(monitor);
	}

	@Override
	public void refresh(String storeId, IProgressMonitor monitor) throws BookmarksException {
		RefreshRemoteFolderOperation operation = new RefreshRemoteFolderOperation(bookmarkDatabase,
				remoteBookmarksStoreManager, bookmarksDirtyStateTracker);
		operation.refresh(storeId, monitor);
	}

	@Override
	public void removeFromRemoteBookmarksStore(String storeId, final BookmarkId bookmarkFolderId,
			final IProgressMonitor monitor) throws BookmarksException {
		RemoveFromRemoteBookmarksStoreOperation operation = new RemoveFromRemoteBookmarksStoreOperation(
				bookmarkDatabase, remoteBookmarksStoreManager);
		operation.removeFromRemoteBookmarksStore(storeId, bookmarkFolderId, monitor);
	}

	@Override
	public void renameBookmark(BookmarkId bookmarkId, String newName) throws BookmarksException {
		RenameBookmarkOperation operation = new RenameBookmarkOperation(bookmarkDatabase,
				bookmarkModificationValidator);
		operation.renameBookmark(bookmarkId, newName);
	}

	@Override
	public void setComment(final BookmarkId bookmarkId, final String comment) throws BookmarksException {
		SetBookmarkCommentOperation operation = new SetBookmarkCommentOperation(bookmarkDatabase,
				bookmarkModificationValidator);
		operation.setComment(bookmarkId, comment);
	}

	@Override
	public void showInBookmarksView(IWorkbenchPage page, BookmarkId bookmarkId) {
		ShowInBookmarksViewOperation operation = new ShowInBookmarksViewOperation(bookmarkDatabase);
		operation.showInBookmarksView(page, bookmarkId);
	}

	@Override
	public void sortByName(BookmarkId bookmarkFolderId) throws BookmarksException {
		SortByNameOperation operation = new SortByNameOperation(bookmarkDatabase, bookmarkModificationValidator);
		operation.sortByName(bookmarkFolderId);
	}

	@Override
	public void updateBookmark(BookmarkId bookmarkId, IWorkbenchPart part, ISelection selection,
			IProgressMonitor monitor) throws BookmarksException {
		UpdateBookmarkOperation operation = new UpdateBookmarkOperation(bookmarkDatabase, bookmarkPropertiesProvider);
		operation.updateBookmark(bookmarkId, part, selection, monitor);
	}

	@Override
	public void gotoBookmark(BookmarkId bookmarkId, IProgressMonitor monitor) throws BookmarksException {
		GotoBookmarkOperation gotoBookmarkOperation = new GotoBookmarkOperation(bookmarkDatabase,
				bookmarkLocationProvider, gotoBookmark);
		gotoBookmarkOperation.gotoBookmark(bookmarkId, monitor);
	}

	@Override
	public void addNumberedBookmark(BookmarkId bookmarkId, BookmarkNumber bookmarkNumber) {
		AddNumberedBookmarkOperation operation = new AddNumberedBookmarkOperation(numberedBookmarks);
		operation.addNumberedBookmark(bookmarkId, bookmarkNumber);
	}

	@Override
	public void gotoNumberedBookmark(BookmarkNumber bookmarkNumber, IProgressMonitor monitor)
			throws BookmarksException {
		GotoNumberedBookmarkOperation operation = new GotoNumberedBookmarkOperation(numberedBookmarks, bookmarkDatabase,
				bookmarkLocationProvider, gotoBookmark);
		operation.gotoNumberedBookmark(bookmarkNumber, monitor);
	}

}
