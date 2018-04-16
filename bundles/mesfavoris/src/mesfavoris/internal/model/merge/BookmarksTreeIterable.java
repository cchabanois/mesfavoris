package mesfavoris.internal.model.merge;

import java.util.Iterator;

import mesfavoris.internal.model.merge.BookmarksTreeIterator.Algorithm;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;

public class BookmarksTreeIterable implements Iterable<Bookmark> {
	private final BookmarksTree bookmarksTree;
	private final BookmarkId bookmarkId;
	private final Algorithm algorithm;
	
	public BookmarksTreeIterable(BookmarksTree bookmarksTree, BookmarkId bookmarkId, Algorithm algorithm) {
		this.bookmarksTree = bookmarksTree;
		this.algorithm = algorithm;
		this.bookmarkId = bookmarkId;
	}

	
	@Override
	public Iterator<Bookmark> iterator() {
		return new BookmarksTreeIterator(bookmarksTree, bookmarkId, algorithm);
	}
	
}
