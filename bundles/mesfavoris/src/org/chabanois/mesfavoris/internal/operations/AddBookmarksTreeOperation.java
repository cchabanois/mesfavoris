package org.chabanois.mesfavoris.internal.operations;

import java.util.List;
import java.util.function.Consumer;

import org.chabanois.mesfavoris.BookmarksException;
import org.chabanois.mesfavoris.internal.model.copy.BookmarksCopier;
import org.chabanois.mesfavoris.internal.model.copy.NonExistingBookmarkIdProvider;
import org.chabanois.mesfavoris.model.BookmarkDatabase;
import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.model.BookmarksTree;
import org.chabanois.mesfavoris.validation.IBookmarkModificationValidator;
import org.eclipse.core.runtime.IStatus;

import com.google.common.collect.Lists;

public class AddBookmarksTreeOperation {
	private final BookmarkDatabase bookmarkDatabase;
	private final IBookmarkModificationValidator bookmarkModificationValidator;

	public AddBookmarksTreeOperation(BookmarkDatabase bookmarkDatabase,
			IBookmarkModificationValidator bookmarkModificationValidator) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.bookmarkModificationValidator = bookmarkModificationValidator;
	}

	public void addBookmarksTree(BookmarkId parentBookmarkId, BookmarksTree sourceBookmarksTree,
			Consumer<BookmarksTree> afterCommit) throws BookmarksException {
		bookmarkDatabase.modify(bookmarksTreeModifier -> {
			IStatus status = bookmarkModificationValidator.validateModification(bookmarksTreeModifier.getCurrentTree(),
					parentBookmarkId);
			if (!status.isOK()) {
				throw new BookmarksException(status);
			}
			BookmarksCopier bookmarksCopier = new BookmarksCopier(sourceBookmarksTree,
					new NonExistingBookmarkIdProvider(bookmarksTreeModifier.getCurrentTree()));
			List<BookmarkId> bookmarkIds = Lists.newArrayList(sourceBookmarksTree.getRootFolder().getId());
			bookmarksCopier.copy(bookmarksTreeModifier, parentBookmarkId, bookmarkIds);
		} , afterCommit);
	}

}
