package mesfavoris.internal.service;

import java.util.List;
import java.util.function.Consumer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;

import mesfavoris.BookmarksException;
import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.internal.operations.AddBookmarkFolderOperation;
import mesfavoris.internal.operations.AddBookmarkOperation;
import mesfavoris.internal.operations.AddBookmarksTreeOperation;
import mesfavoris.internal.operations.AddToRemoteBookmarksStoreOperation;
import mesfavoris.internal.operations.ConnectToRemoteBookmarksStoreOperation;
import mesfavoris.internal.operations.CopyBookmarkOperation;
import mesfavoris.internal.operations.CutBookmarkOperation;
import mesfavoris.internal.operations.DeleteBookmarksOperation;
import mesfavoris.internal.operations.GetLinkedBookmarksOperation;
import mesfavoris.internal.operations.PasteBookmarkOperation;
import mesfavoris.internal.operations.RefreshRemoteFolderOperation;
import mesfavoris.internal.operations.RemoveFromRemoteBookmarksStoreOperation;
import mesfavoris.internal.operations.RenameBookmarkOperation;
import mesfavoris.internal.operations.SetBookmarkCommentOperation;
import mesfavoris.internal.operations.ShowInBookmarksViewOperation;
import mesfavoris.internal.operations.SortByNameOperation;
import mesfavoris.internal.operations.UpdateBookmarkOperation;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.persistence.IBookmarksDatabaseDirtyStateTracker;
import mesfavoris.remote.RemoteBookmarksStoreManager;
import mesfavoris.service.IBookmarksService;
import mesfavoris.validation.IBookmarkModificationValidator;
import mesfavoris.workspace.DefaultBookmarkFolderProvider;

public class BookmarksService implements IBookmarksService {
	private final BookmarkDatabase bookmarkDatabase;
	private final IBookmarkModificationValidator bookmarkModificationValidator;
	private final IBookmarkPropertiesProvider bookmarkPropertiesProvider;
	private final DefaultBookmarkFolderProvider defaultBookmarkFolderProvider;
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;
	private final IBookmarksDatabaseDirtyStateTracker bookmarksDatabaseDirtyStateTracker;

	public BookmarksService(BookmarkDatabase bookmarkDatabase,
			IBookmarkModificationValidator bookmarkModificationValidator,
			IBookmarkPropertiesProvider bookmarkPropertiesProvider,
			DefaultBookmarkFolderProvider defaultBookmarkFolderProvider,
			RemoteBookmarksStoreManager remoteBookmarksStoreManager,
			IBookmarksDatabaseDirtyStateTracker bookmarksDatabaseDirtyStateTracker) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.bookmarkModificationValidator = bookmarkModificationValidator;
		this.bookmarkPropertiesProvider = bookmarkPropertiesProvider;
		this.defaultBookmarkFolderProvider = defaultBookmarkFolderProvider;
		this.remoteBookmarksStoreManager = remoteBookmarksStoreManager;
		this.bookmarksDatabaseDirtyStateTracker = bookmarksDatabaseDirtyStateTracker;
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
				remoteBookmarksStoreManager, bookmarksDatabaseDirtyStateTracker);
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
				remoteBookmarksStoreManager, bookmarksDatabaseDirtyStateTracker);
		operation.refresh(bookmarkFolderId, monitor);
	}

	@Override
	public void refresh(IProgressMonitor monitor) throws BookmarksException {
		RefreshRemoteFolderOperation operation = new RefreshRemoteFolderOperation(bookmarkDatabase,
				remoteBookmarksStoreManager, bookmarksDatabaseDirtyStateTracker);
		operation.refresh(monitor);
	}

	@Override
	public void refresh(String storeId, IProgressMonitor monitor) throws BookmarksException {
		RefreshRemoteFolderOperation operation = new RefreshRemoteFolderOperation(bookmarkDatabase,
				remoteBookmarksStoreManager, bookmarksDatabaseDirtyStateTracker);
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

}
