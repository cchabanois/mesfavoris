package mesfavoris.internal.model.merge;

import java.util.Iterator;
import java.util.function.Predicate;

import mesfavoris.internal.model.merge.BookmarksTreeIterator.Algorithm;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;

public class BookmarksTreeIterable implements Iterable<Bookmark> {
	private final BookmarksTree bookmarksTree;
	private final BookmarkId bookmarkId;
	private final Algorithm algorithm;
	private Predicate<Bookmark> filter;

	public BookmarksTreeIterable(BookmarksTree bookmarksTree, BookmarkId bookmarkId, Algorithm algorithm) {
		this(bookmarksTree, bookmarkId, algorithm, x -> true);
	}
	
	public BookmarksTreeIterable(BookmarksTree bookmarksTree, BookmarkId bookmarkId, Algorithm algorithm, Predicate<Bookmark> filter) {
		this.bookmarksTree = bookmarksTree;
		this.algorithm = algorithm;
		this.filter = filter;
		this.bookmarkId = bookmarkId;
	}

	
	@Override
	public Iterator<Bookmark> iterator() {
		return new BookmarksTreeIterator(bookmarksTree, bookmarkId, algorithm, filter);
	}
	
}
