package mesfavoris.internal.service.operations;

import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.Lists;

import mesfavoris.BookmarksException;
import mesfavoris.internal.model.copy.BookmarksCopier;
import mesfavoris.internal.model.copy.NonExistingBookmarkIdProvider;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;

public class AddBookmarksTreeOperation {
	private final BookmarkDatabase bookmarkDatabase;

	public AddBookmarksTreeOperation(BookmarkDatabase bookmarkDatabase) {
		this.bookmarkDatabase = bookmarkDatabase;
	}

	public void addBookmarksTree(BookmarkId parentBookmarkId, BookmarksTree sourceBookmarksTree,
			Consumer<BookmarksTree> afterCommit) throws BookmarksException {
		bookmarkDatabase.modify(bookmarksTreeModifier -> {
			BookmarksCopier bookmarksCopier = new BookmarksCopier(sourceBookmarksTree,
					new NonExistingBookmarkIdProvider(bookmarksTreeModifier.getCurrentTree()));
			List<BookmarkId> bookmarkIds = Lists.newArrayList(sourceBookmarksTree.getRootFolder().getId());
			bookmarksCopier.copy(bookmarksTreeModifier, parentBookmarkId, bookmarkIds);
		} , afterCommit);
	}

}
