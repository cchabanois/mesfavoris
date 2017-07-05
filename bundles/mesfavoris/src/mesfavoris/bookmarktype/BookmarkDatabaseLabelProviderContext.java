package mesfavoris.bookmarktype;

import java.util.function.Supplier;

import mesfavoris.bookmarktype.IBookmarkLabelProvider.Context;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarksTree;

public class BookmarkDatabaseLabelProviderContext implements Context {
	private final String bookmarkDatabaseId;
	private final Supplier<BookmarksTree> bookmarksTreeProvider;

	public BookmarkDatabaseLabelProviderContext(BookmarkDatabase bookmarkDatabase) {
		this(bookmarkDatabase.getId(), ()->bookmarkDatabase.getBookmarksTree());
	}

	public BookmarkDatabaseLabelProviderContext(String bookmarkDatabaseId, Supplier<BookmarksTree> bookmarksTreeProvider) {
		this.bookmarkDatabaseId = bookmarkDatabaseId;
		this.bookmarksTreeProvider = bookmarksTreeProvider;
	}
	
	@Override
	public <T> T get(String name) {
		switch (name) {
		case Context.BOOKMARK_DATABASE_ID:
			return (T) bookmarkDatabaseId;
		case Context.BOOKMARKS_TREE:
			return (T) bookmarksTreeProvider.get();
		default:
			return null;
		}
	}

}
