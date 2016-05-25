package org.chabanois.mesfavoris.internal.model.merge;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import org.chabanois.mesfavoris.model.Bookmark;
import org.chabanois.mesfavoris.model.BookmarkFolder;
import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.model.BookmarksTree;

public class BookmarksTreeIterable implements Iterable<Bookmark> {
	private final BookmarksTree bookmarksTree;
	private final List<Bookmark> orderedBookmarksList = new ArrayList<>();
	private final Algorithm algorithm;
	private Predicate<Bookmark> filter;
	
	public static enum Algorithm {
		PRE_ORDER,
		POST_ORDER
	}
	public BookmarksTreeIterable(BookmarksTree bookmarksTree, BookmarkId bookmarkFolderId, Algorithm algorithm) {
		this(bookmarksTree, bookmarkFolderId, algorithm, x -> true);
	}
	
	public BookmarksTreeIterable(BookmarksTree bookmarksTree, BookmarkId bookmarkFolderId, Algorithm algorithm, Predicate<Bookmark> filter) {
		this.bookmarksTree = bookmarksTree;
		this.algorithm = algorithm;
		this.filter = filter;
		getOrdereredList(bookmarksTree.getBookmark(bookmarkFolderId), orderedBookmarksList);
	}

	private void getOrdereredList(Bookmark bookmark, List<Bookmark> result) {
		if (algorithm == Algorithm.PRE_ORDER && filter.test(bookmark)) {
			result.add(bookmark);
		}
		if (bookmark instanceof BookmarkFolder) {
			for (Bookmark child : bookmarksTree.getChildren(bookmark.getId())) {
				getOrdereredList(child, result);	
			}
		}
		if (algorithm == Algorithm.POST_ORDER && filter.test(bookmark)) {
			result.add(bookmark);
		}
	}
	
	@Override
	public Iterator<Bookmark> iterator() {
		return orderedBookmarksList.iterator();
	}

}
