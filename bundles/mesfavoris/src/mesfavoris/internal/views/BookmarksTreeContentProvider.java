package mesfavoris.internal.views;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkFolder;

public class BookmarksTreeContentProvider implements ITreeContentProvider {
	private BookmarkDatabase bookmarkDatabase;

	public BookmarksTreeContentProvider(BookmarkDatabase bookmarkDatabase) {
		this.bookmarkDatabase = bookmarkDatabase;
	}
	
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {

	}

	public void dispose() {
	}

	public Object[] getElements(final Object parent) {
		BookmarkFolder bookmarkFolder = (BookmarkFolder)parent;
		return bookmarkDatabase.getBookmarksTree().getChildren(bookmarkFolder.getId()).toArray();
	}

	public Object getParent(final Object child) {
		if (child instanceof Bookmark) {
			try {
				Bookmark bookmark = (Bookmark) child;
				return bookmarkDatabase.getBookmarksTree().getParentBookmark(bookmark.getId());
			} catch (IllegalArgumentException e) {
				// happens when child has been removed and tree is being
				// refreshed
				return null;
			}
		}
		return null;
	}

	public Object[] getChildren(final Object parent) {
		if (parent instanceof BookmarkFolder) {
			BookmarkFolder bookmarkFolder = (BookmarkFolder) parent;
			return bookmarkDatabase.getBookmarksTree().getChildren(bookmarkFolder.getId()).toArray();
		}
		return new Object[0];
	}

	public boolean hasChildren(final Object parent) {
		if (parent instanceof BookmarkFolder) {
			BookmarkFolder bookmarkFolder = (BookmarkFolder) parent;
			return bookmarkDatabase.getBookmarksTree().getChildren(bookmarkFolder.getId()).size() > 0;
		}
		return false;
	}

}