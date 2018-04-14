package mesfavoris.internal.handlers;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import mesfavoris.BookmarksException;
import mesfavoris.MesFavoris;
import mesfavoris.handlers.AbstractBookmarkHandler;
import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarksTree;
import mesfavoris.remote.RemoteBookmarksStoreManager;
import mesfavoris.ui.viewers.BookmarksTableLabelProvider;

public class DeleteBookmarkHandler extends AbstractBookmarkHandler {
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;
	private final BookmarkDatabase bookmarkDatabase;
	
	public DeleteBookmarkHandler() {
		bookmarkDatabase = MesFavoris.getBookmarkDatabase();
		remoteBookmarksStoreManager = BookmarksPlugin.getDefault().getRemoteBookmarksStoreManager();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		if (selection.isEmpty()) {
			return null;
		}
		List<Bookmark> bookmarks = new ArrayList<>(
				getBookmarksToDelete(bookmarkDatabase.getBookmarksTree(), selection));
		ConfirmDeleteDialog dialog = new ConfirmDeleteDialog(HandlerUtil.getActiveShell(event), bookmarks);
		if (dialog.open() != Window.OK) {
			return null;
		}

		try {
			bookmarksService.deleteBookmarks(getAsBookmarkIds(selection), true);
		} catch (BookmarksException e) {
			throw new ExecutionException("Could not delete bookmark", e);
		}
		return null;
	}

	private Set<Bookmark> getBookmarksToDelete(BookmarksTree bookmarksTree, IStructuredSelection selection) {
		Set<Bookmark> bookmarks = new LinkedHashSet<>();
		for (Bookmark bookmark : ((List<Bookmark>) (selection.toList()))) {
			bookmarks.add(bookmark);
			if (bookmark instanceof BookmarkFolder
					&& remoteBookmarksStoreManager.getRemoteBookmarkFolder(bookmark.getId()).isPresent()) {
				bookmarks.addAll(getBookmarksRecursively(bookmarksTree, bookmark.getId(), b->true));
			}
		}
		return bookmarks;
	}

	private static class ConfirmDeleteDialog extends MessageDialog {
		private final List<Bookmark> bookmarks;

		public ConfirmDeleteDialog(Shell parentShell, List<Bookmark> bookmarks) {
			super(parentShell, "Delete bookmarks", null, "Are you sure you want to delete these bookmarks ?",
					MessageDialog.CONFIRM, new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL },
					0);
			setShellStyle(getShellStyle() | SWT.RESIZE | SWT.SHEET);
			this.bookmarks = bookmarks;
		}

		@Override
		protected Control createCustomArea(Composite parent) {
			Composite area = new Composite(parent, SWT.NONE);
			area.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
			area.setLayout(new FillLayout());

			TableViewer tableViewer = new TableViewer(area);
			tableViewer.setContentProvider(new ArrayContentProvider());
			BookmarksTableLabelProvider labelProvider = new BookmarksTableLabelProvider(
					MesFavoris.getBookmarkDatabase(), BookmarksPlugin.getDefault().getRemoteBookmarksStoreManager(),
					BookmarksPlugin.getDefault().getBookmarkLabelProvider());
			tableViewer.setLabelProvider(labelProvider);
			tableViewer.setInput(bookmarks);

			return area;
		}

	}

}
