package mesfavoris.internal.views.details;

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.model.compare.BookmarkComparer;
import mesfavoris.internal.model.compare.BookmarkComparer.BookmarkDifferences;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.IBookmarksListener;
import mesfavoris.model.modification.BookmarksModification;
import mesfavoris.ui.details.IBookmarkDetailPart;

public abstract class AbstractBookmarkDetailPart implements IBookmarkDetailPart {
	protected final BookmarkDatabase bookmarkDatabase;
	protected Bookmark bookmark;
	private final IBookmarksListener bookmarksListener = (modifications) -> handleModifications(modifications);

	public AbstractBookmarkDetailPart() {
		this.bookmarkDatabase = BookmarksPlugin.getDefault().getBookmarkDatabase();
	}

	@Override
	public void createControl(Composite parent, FormToolkit formToolkit) {
		bookmarkDatabase.addListener(bookmarksListener);
	}
	
	@Override
	public void setBookmark(Bookmark bookmark) {
		this.bookmark = bookmark;
	}

	@Override
	public void dispose() {
		bookmarkDatabase.removeListener(bookmarksListener);
	}

	private void handleModifications(List<BookmarksModification> modifications) {
		if (!canHandle(bookmark) || bookmark == null) {
			return;
		}
		Bookmark newBookmark = bookmarkDatabase.getBookmarksTree().getBookmark(bookmark.getId());
		BookmarkComparer bookmarkComparer = new BookmarkComparer();
		BookmarkDifferences bookmarkDifferences = bookmarkComparer.compare(bookmark, newBookmark);
		if (bookmarkDifferences.isEmpty()) {
			return;
		}
		Bookmark oldBookmark = bookmark;
		bookmark = newBookmark;
		bookmarkModified(oldBookmark, bookmark);
	}

	protected abstract void bookmarkModified(Bookmark oldBookmark, Bookmark newBookmark);

}
