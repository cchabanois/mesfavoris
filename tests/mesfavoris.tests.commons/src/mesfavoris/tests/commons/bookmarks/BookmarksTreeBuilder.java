package mesfavoris.tests.commons.bookmarks;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;

public class BookmarksTreeBuilder {
	private BookmarksTree bookmarksTree;

	public BookmarksTreeBuilder(String rootFolderName) {
		this.bookmarksTree = new BookmarksTree(new BookmarkFolder(new BookmarkId(rootFolderName), rootFolderName));
	}

	@SafeVarargs
	public final BookmarksTreeBuilder addBookmarks(String parentId, BookmarkBuilder<? extends Bookmark>... bookmarks) {
		return addBookmarks(new BookmarkId(parentId), bookmarks);
	}

	@SafeVarargs
	public final BookmarksTreeBuilder addBookmarks(BookmarkId parentId, BookmarkBuilder<? extends Bookmark>... bookmarks) {
		bookmarksTree = bookmarksTree.addBookmarks(parentId,
				Stream.of(bookmarks).map(bookmarkBuilder -> bookmarkBuilder.build()).collect(Collectors.toList()));
		return this;
	}

	public BookmarksTree build() {
		return bookmarksTree;
	}

	public static BookmarksTreeBuilder bookmarksTree(String rootFolderName) {
		return new BookmarksTreeBuilder(rootFolderName);
	}

}
