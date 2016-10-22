package mesfavoris.service;

import java.util.List;
import java.util.function.Consumer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;

import mesfavoris.BookmarksException;
import mesfavoris.internal.numberedbookmarks.BookmarkNumber;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;

public interface IBookmarksService {

	BookmarksTree getBookmarksTree();

	void addBookmarkFolder(BookmarkId parentFolderId, String folderName) throws BookmarksException;

	BookmarkId addBookmark(IWorkbenchPart part, ISelection selection, IProgressMonitor monitor)
			throws BookmarksException;

	void addBookmarksTree(BookmarkId parentBookmarkId, BookmarksTree sourceBookmarksTree,
			Consumer<BookmarksTree> afterCommit) throws BookmarksException;

	void addToRemoteBookmarksStore(String storeId, BookmarkId bookmarkFolderId, IProgressMonitor monitor)
			throws BookmarksException;

	void connectToRemoteBookmarksStore(String storeId, IProgressMonitor monitor) throws BookmarksException;

	void copyToClipboard(List<BookmarkId> selection);

	void cutToClipboard(List<BookmarkId> selection) throws BookmarksException;

	void deleteBookmarks(List<BookmarkId> selection) throws BookmarksException;

	List<Bookmark> getLinkedBookmarks(IWorkbenchPart part, ISelection selection);

	void paste(Display display, BookmarkId parentBookmarkId, IProgressMonitor monitor) throws BookmarksException;

	void refresh(BookmarkId bookmarkFolderId, IProgressMonitor monitor) throws BookmarksException;

	void refresh(IProgressMonitor monitor) throws BookmarksException;

	void refresh(String storeId, IProgressMonitor monitor) throws BookmarksException;

	void removeFromRemoteBookmarksStore(String storeId, BookmarkId bookmarkFolderId, IProgressMonitor monitor)
			throws BookmarksException;

	void renameBookmark(BookmarkId bookmarkId, String newName) throws BookmarksException;

	void setComment(BookmarkId bookmarkId, String comment) throws BookmarksException;

	void showInBookmarksView(IWorkbenchPage page, BookmarkId bookmarkId, boolean activate);

	void sortByName(BookmarkId bookmarkFolderId) throws BookmarksException;

	void updateBookmark(BookmarkId bookmarkId, IWorkbenchPart part, ISelection selection, IProgressMonitor monitor)
			throws BookmarksException;

	void gotoBookmark(BookmarkId bookmarkId, IProgressMonitor monitor) throws BookmarksException;

	void addNumberedBookmark(BookmarkId bookmarkId, BookmarkNumber bookmarkNumber);
	
	void gotoNumberedBookmark(BookmarkNumber bookmarkNumber, IProgressMonitor monitor) throws BookmarksException;

}
