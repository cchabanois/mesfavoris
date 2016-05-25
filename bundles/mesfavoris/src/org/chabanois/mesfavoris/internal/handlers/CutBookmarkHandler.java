package org.chabanois.mesfavoris.internal.handlers;

import java.util.List;

import org.chabanois.mesfavoris.BookmarksException;
import org.chabanois.mesfavoris.BookmarksPlugin;
import org.chabanois.mesfavoris.internal.operations.CutBookmarkOperation;
import org.chabanois.mesfavoris.model.BookmarkDatabase;
import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.validation.BookmarkModificationValidator;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class CutBookmarkHandler extends AbstractBookmarkHandler {
	private final BookmarkDatabase bookmarkDatabase;
	private final BookmarkModificationValidator bookmarkModificationValidator;

	public CutBookmarkHandler() {
		this.bookmarkDatabase = BookmarksPlugin.getBookmarkDatabase();
		this.bookmarkModificationValidator = new BookmarkModificationValidator(
				BookmarksPlugin.getRemoteBookmarksStoreManager());
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		if (selection.isEmpty()) {
			return null;
		}
		try {
			CutBookmarkOperation operation = new CutBookmarkOperation(bookmarkDatabase, bookmarkModificationValidator);
			operation.cutToClipboard(getAsBookmarkIds(selection));
		} catch (BookmarksException e) {
			throw new ExecutionException("Could not cut bookmarks to clipboard", e);
		}
		return null;
	}

	@Override
	public boolean isEnabled() {
		List<BookmarkId> bookmarkIds = getAsBookmarkIds(getSelection());
		CutBookmarkOperation operation = new CutBookmarkOperation(bookmarkDatabase, bookmarkModificationValidator);
		return operation.hasDuplicatedBookmarksInSelection(bookmarkDatabase.getBookmarksTree(), bookmarkIds);
	}
}
