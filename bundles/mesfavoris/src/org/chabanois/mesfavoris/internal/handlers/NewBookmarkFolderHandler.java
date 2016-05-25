package org.chabanois.mesfavoris.internal.handlers;

import org.chabanois.mesfavoris.BookmarksException;
import org.chabanois.mesfavoris.BookmarksPlugin;
import org.chabanois.mesfavoris.internal.operations.AddBookmarkFolderOperation;
import org.chabanois.mesfavoris.model.Bookmark;
import org.chabanois.mesfavoris.model.BookmarkDatabase;
import org.chabanois.mesfavoris.model.BookmarkFolder;
import org.chabanois.mesfavoris.validation.BookmarkModificationValidator;
import org.chabanois.mesfavoris.validation.IBookmarkModificationValidator;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

public class NewBookmarkFolderHandler extends AbstractHandler {
	private final BookmarkDatabase bookmarkDatabase;
	private final IBookmarkModificationValidator bookmarkModificationValidator;

	public NewBookmarkFolderHandler() {
		this.bookmarkDatabase = BookmarksPlugin.getBookmarkDatabase();
		this.bookmarkModificationValidator = new BookmarkModificationValidator(
				BookmarksPlugin.getRemoteBookmarksStoreManager());
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		BookmarkFolder parent = null;
		if (selection.isEmpty()) {
			parent = bookmarkDatabase.getBookmarksTree().getRootFolder();
		} else {
			Bookmark bookmark = (Bookmark) selection.getFirstElement();
			if (!(bookmark instanceof BookmarkFolder)) {
				return null;
			}
			parent = (BookmarkFolder) bookmark;
		}
		Shell shell = HandlerUtil.getActiveShell(event);
		String folderName = askBookmarkFolderName(shell);
		if (folderName == null) {
			return null;
		}
		AddBookmarkFolderOperation operation = new AddBookmarkFolderOperation(bookmarkDatabase,
				bookmarkModificationValidator);
		try {
			operation.addBookmarkFolder(parent.getId(), folderName);
		} catch (BookmarksException e) {
			throw new ExecutionException("Could not add bookmark folder", e);
		}

		return null;
	}

	private String askBookmarkFolderName(Shell parentShell) {
		InputDialog inputDialog = new InputDialog(parentShell, "New folder", "Enter folder name", "untitled", null);
		if (inputDialog.open() == Window.OK) {
			return inputDialog.getValue();
		}
		return null;
	}

}
