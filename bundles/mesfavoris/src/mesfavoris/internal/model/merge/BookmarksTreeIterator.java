package mesfavoris.internal.model.merge;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;

public class BookmarksTreeIterator implements Iterator<Bookmark> {
	private final BookmarksTree bookmarksTree;
	private final Algorithm algorithm;
	private final Deque<BookmarkIterator> stack = new ArrayDeque<BookmarkIterator>();

	public static enum Algorithm {
		PRE_ORDER, POST_ORDER
	}

	public BookmarksTreeIterator(BookmarksTree bookmarksTree, BookmarkId bookmarkId, Algorithm algorithm) {
		this.bookmarksTree = bookmarksTree;
		this.algorithm = algorithm;
		stack.push(new BookmarkIterator(bookmarksTree.getBookmark(bookmarkId), true));
	}

	@Override
	public boolean hasNext() {
		return getCurrentIterator().map(it->it.hasNext()).orElse(false);
	}

	private Optional<BookmarkIterator> getCurrentIterator() {
		BookmarkIterator currentIterator  = null;
		do {
			if (stack.isEmpty()) {
				return Optional.empty();
			}
			currentIterator = stack.peek();
			if (!currentIterator.hasNext()) {
				stack.pop();
			}
		} while (!currentIterator.hasNext());
		return Optional.of(currentIterator);
	}
	
	@Override
	public Bookmark next() {
		Bookmark nextBookmark = null;
		while (nextBookmark == null) {
			Bookmark bookmark = null;
			BookmarkIterator currentIterator  = getCurrentIterator().orElseThrow(()->new NoSuchElementException());			
			bookmark = currentIterator.next();
			if (bookmark instanceof BookmarkFolder && currentIterator.goInside) {
				if (algorithm == Algorithm.PRE_ORDER) {
					nextBookmark = bookmark;
				}
				if (algorithm == Algorithm.POST_ORDER) {
					stack.push(new BookmarkIterator(bookmark, false));
				}
				stack.push(new BookmarkIterator(bookmarksTree.getChildren(bookmark.getId()).iterator(), true));
			} else {
				nextBookmark = bookmark;
			}
		}
		return nextBookmark;
	}

	private static class BookmarkIterator implements Iterator<Bookmark> {
		private final Iterator<Bookmark> iterator;
		private final boolean goInside;

		public BookmarkIterator(Bookmark bookmark, boolean goInside) {
			this(Collections.singleton(bookmark).iterator(), goInside);
		}

		public BookmarkIterator(Iterator<Bookmark> iterator, boolean goInside) {
			this.iterator = iterator;
			this.goInside = goInside;
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public Bookmark next() {
			return iterator.next();
		}

	}

}
