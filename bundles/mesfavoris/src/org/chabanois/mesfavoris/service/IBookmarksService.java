package org.chabanois.mesfavoris.service;

import java.util.function.Consumer;

import org.chabanois.mesfavoris.BookmarksException;
import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.model.BookmarksTree;

public interface IBookmarksService {

	BookmarksTree getBookmarksTree();
	
	void addBookmarksTree(BookmarkId parentBookmarkId, BookmarksTree sourceBookmarksTree, Consumer<BookmarksTree> afterCommit) throws BookmarksException;

}
