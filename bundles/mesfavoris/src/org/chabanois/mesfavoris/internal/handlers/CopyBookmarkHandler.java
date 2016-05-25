package org.chabanois.mesfavoris.internal.handlers;

import java.util.List;

import org.chabanois.mesfavoris.BookmarksPlugin;
import org.chabanois.mesfavoris.internal.operations.CopyBookmarkOperation;
import org.chabanois.mesfavoris.model.BookmarkDatabase;
import org.chabanois.mesfavoris.model.BookmarkId;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class CopyBookmarkHandler extends AbstractBookmarkHandler {
	private final BookmarkDatabase bookmarkDatabase;

	public CopyBookmarkHandler() {
		this.bookmarkDatabase = BookmarksPlugin.getBookmarkDatabase();
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		if (selection.isEmpty()) {
			return null;
		}
		List<BookmarkId> bookmarkIds = getAsBookmarkIds(selection);
		CopyBookmarkOperation operation = new CopyBookmarkOperation();
		operation.copyToClipboard(bookmarkDatabase.getBookmarksTree(), bookmarkIds);
		return null;
	}

	@Override
	public boolean isEnabled() {
		List<BookmarkId> bookmarkIds = getAsBookmarkIds(getSelection());
		CopyBookmarkOperation operation = new CopyBookmarkOperation();
		return !operation.hasDuplicatedBookmarksInSelection(bookmarkDatabase.getBookmarksTree(), bookmarkIds);
	}

}