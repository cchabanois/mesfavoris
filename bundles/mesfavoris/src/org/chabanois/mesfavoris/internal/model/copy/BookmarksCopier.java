package org.chabanois.mesfavoris.internal.model.copy;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.chabanois.mesfavoris.model.Bookmark;
import org.chabanois.mesfavoris.model.BookmarkFolder;
import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.model.BookmarksTree;
import org.chabanois.mesfavoris.model.modification.IBookmarksTreeModifier;

/**
 * Copy bookmarks from one {@link BookmarksTree} to another.
 * 
 * @author cchabanois
 *
 */
public class BookmarksCopier {
	private final BookmarksTree sourceTree;
	private final IBookmarkCopyIdProvider bookmarkCopyIdProvider;

	public BookmarksCopier(BookmarksTree sourceTree, IBookmarkCopyIdProvider bookmarkCopyIdProvider) {
		this.sourceTree = sourceTree;
		this.bookmarkCopyIdProvider = bookmarkCopyIdProvider;
	}

	public void copy(IBookmarksTreeModifier bookmarksTreeModifier, BookmarkId targetBookmarkFolderId,
			List<BookmarkId> bookmarkIds) {
		copyBookmarks(bookmarksTreeModifier, targetBookmarkFolderId, getUnduplicatedSourceBookmarks(bookmarkIds));
	}

	public void copyAfter(IBookmarksTreeModifier bookmarksTreeModifier, BookmarkId targetBookmarkFolderId, BookmarkId existingBookmarkId,
			List<BookmarkId> bookmarkIds) {
		copyBookmarksAfter(bookmarksTreeModifier, targetBookmarkFolderId, existingBookmarkId, getUnduplicatedSourceBookmarks(bookmarkIds));
	}	
	
	private void copyBookmarksAfter(IBookmarksTreeModifier bookmarksTreeModifier, BookmarkId targetBookmarkFolderId, BookmarkId existingBookmarkId,
			List<Bookmark> bookmarks) {
		List<Bookmark> bookmarksCopy = copyBookmarks(bookmarks);
		bookmarksTreeModifier.addBookmarksAfter(targetBookmarkFolderId, existingBookmarkId, bookmarksCopy);
		Iterator<Bookmark> bookmarksIt = bookmarks.iterator();
		Iterator<Bookmark> bookmarksCopyIt = bookmarksCopy.iterator();
		while (bookmarksIt.hasNext() && bookmarksCopyIt.hasNext()) {
			Bookmark bookmark = bookmarksIt.next();
			Bookmark bookmarkCopy = bookmarksCopyIt.next();
			if (bookmark instanceof BookmarkFolder) {
				List<Bookmark> children = sourceTree.getChildren(bookmark.getId());
				copyBookmarks(bookmarksTreeModifier, bookmarkCopy.getId(), children);
			}
		}
	}	
	
	private void copyBookmarks(IBookmarksTreeModifier bookmarksTreeModifier, BookmarkId targetBookmarkFolderId,
			List<Bookmark> bookmarks) {
		List<Bookmark> bookmarksCopy = copyBookmarks(bookmarks);
		bookmarksTreeModifier.addBookmarks(targetBookmarkFolderId, bookmarksCopy);
		Iterator<Bookmark> bookmarksIt = bookmarks.iterator();
		Iterator<Bookmark> bookmarksCopyIt = bookmarksCopy.iterator();
		while (bookmarksIt.hasNext() && bookmarksCopyIt.hasNext()) {
			Bookmark bookmark = bookmarksIt.next();
			Bookmark bookmarkCopy = bookmarksCopyIt.next();
			if (bookmark instanceof BookmarkFolder) {
				List<Bookmark> children = sourceTree.getChildren(bookmark.getId());
				copyBookmarks(bookmarksTreeModifier, bookmarkCopy.getId(), children);
			}
		}
	}

	private List<Bookmark> copyBookmarks(List<Bookmark> bookmarks) {
		return bookmarks.stream().map(b -> copyBookmark(b)).collect(Collectors.toList());
	}

	private Bookmark copyBookmark(Bookmark bookmark) {
		BookmarkId bookmarkId = bookmarkCopyIdProvider.getBookmarkCopyId(bookmark.getId());
		if (bookmark.getId().equals(bookmarkId)) {
			return bookmark;
		}
		if (bookmark instanceof BookmarkFolder) {
			return new BookmarkFolder(bookmarkId, bookmark.getProperties());
		} else {
			return new Bookmark(bookmarkId, bookmark.getProperties());
		}
	}

	private List<Bookmark> getUnduplicatedSourceBookmarks(List<BookmarkId> selection) {
		List<Bookmark> bookmarks = selection.stream().map(b -> sourceTree.getBookmark(b)).collect(Collectors.toList());
		List<BookmarkFolder> bookmarkFolders = bookmarks.stream().filter(b -> b instanceof BookmarkFolder)
				.map(b -> (BookmarkFolder) b).collect(Collectors.toList());
		List<Bookmark> result = bookmarks.stream().filter(b -> !isSourceBookmarkUnderAny(b, bookmarkFolders))
				.collect(Collectors.toList());
		return result;
	}

	private boolean isSourceBookmarkUnderAny(Bookmark bookmark, List<BookmarkFolder> bookmarkFolders) {
		for (BookmarkFolder bookmarkFolder : bookmarkFolders) {
			if (isSourceBookmarkUnder(bookmark, bookmarkFolder)) {
				return true;
			}
		}
		return false;
	}

	private boolean isSourceBookmarkUnder(Bookmark bookmark, BookmarkFolder bookmarkFolder) {
		if (sourceTree.getChildren(bookmarkFolder.getId()).contains(bookmark)) {
			return true;
		}
		BookmarkFolder parent = sourceTree.getParentBookmark(bookmark.getId());
		if (parent == null) {
			return false;
		}
		return isSourceBookmarkUnder(parent, bookmarkFolder);
	}

}
