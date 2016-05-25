package org.chabanois.mesfavoris.internal.handlers;

import org.chabanois.mesfavoris.BookmarksException;
import org.chabanois.mesfavoris.BookmarksPlugin;
import org.chabanois.mesfavoris.internal.operations.PasteBookmarkOperation;
import org.chabanois.mesfavoris.model.BookmarkDatabase;
import org.chabanois.mesfavoris.model.BookmarkFolder;
import org.chabanois.mesfavoris.validation.BookmarkModificationValidator;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class PasteBookmarkHandler extends AbstractBookmarkHandler {
	private final BookmarkDatabase bookmarkDatabase;
	private final BookmarkModificationValidator bookmarkModificationValidator;

	public PasteBookmarkHandler() {
		this.bookmarkDatabase = BookmarksPlugin.getBookmarkDatabase();
		this.bookmarkModificationValidator = new BookmarkModificationValidator(
				BookmarksPlugin.getRemoteBookmarksStoreManager());
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		if (selection.size() != 1 || !(selection.getFirstElement() instanceof BookmarkFolder)) {
			return null;
		}
		BookmarkFolder bookmarkFolder = (BookmarkFolder) selection.getFirstElement();
		PasteBookmarkOperation operation = new PasteBookmarkOperation(bookmarkDatabase, bookmarkModificationValidator);
		try {
			operation.paste(bookmarkFolder.getId());
		} catch (BookmarksException e) {
			throw new ExecutionException("Could not paste bookmarks", e);
		}

		return null;
	}

}
