package org.chabanois.mesfavoris.internal.service;

import java.util.function.Consumer;

import org.chabanois.mesfavoris.BookmarksException;
import org.chabanois.mesfavoris.internal.operations.AddBookmarksTreeOperation;
import org.chabanois.mesfavoris.model.BookmarkDatabase;
import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.model.BookmarksTree;
import org.chabanois.mesfavoris.service.IBookmarksService;
import org.chabanois.mesfavoris.validation.IBookmarkModificationValidator;

public class BookmarksService implements IBookmarksService {
	private final BookmarkDatabase bookmarkDatabase;
	private final IBookmarkModificationValidator bookmarkModificationValidator;

	public BookmarksService(BookmarkDatabase bookmarkDatabase,
			IBookmarkModificationValidator bookmarkModificationValidator) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.bookmarkModificationValidator = bookmarkModificationValidator;
	}

	@Override
	public BookmarksTree getBookmarksTree() {
		return bookmarkDatabase.getBookmarksTree();
	}
	
	public void addBookmarksTree(BookmarkId parentBookmarkId, BookmarksTree sourceBookmarksTree,
			Consumer<BookmarksTree> afterCommit) throws BookmarksException {
		AddBookmarksTreeOperation operation = new AddBookmarksTreeOperation(bookmarkDatabase,
				bookmarkModificationValidator);
		operation.addBookmarksTree(parentBookmarkId, sourceBookmarksTree, afterCommit);
	}

}
