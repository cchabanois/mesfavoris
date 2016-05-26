package mesfavoris.service;

import java.util.function.Consumer;

import mesfavoris.BookmarksException;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;

public interface IBookmarksService {

	BookmarksTree getBookmarksTree();
	
	void addBookmarksTree(BookmarkId parentBookmarkId, BookmarksTree sourceBookmarksTree, Consumer<BookmarksTree> afterCommit) throws BookmarksException;

}
