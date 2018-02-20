package mesfavoris.internal.views.comment;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;

import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.views.BookmarksView;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.ui.details.IBookmarkDetailPart;

/**
 * Create component to display bookmarks comments
 * 
 * @author cchabanois
 *
 */
public class CommentBookmarkDetailPart implements IBookmarkDetailPart {
	private final BookmarkDatabase bookmarkDatabase;
	private BookmarksView bookmarksView;
	private BookmarkCommentArea bookmarkCommentArea;
	
	public CommentBookmarkDetailPart() {
		this.bookmarkDatabase = BookmarksPlugin.getDefault().getBookmarkDatabase();
	}

	@Override
	public void initialize(BookmarksView bookmarksView) {
		this.bookmarksView = bookmarksView;
	}

	@Override
	public void createControl(Composite parent) {
		bookmarkCommentArea = new BookmarkCommentArea(parent,
				SWT.MULTI | SWT.V_SCROLL | SWT.WRAP | bookmarksView.getFormToolkit().getBorderStyle(),
				bookmarkDatabase);
		bookmarkCommentArea.getTextWidget().setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		bookmarkCommentArea.setBookmark(null);
	}

	@Override
	public Control getControl() {
		return bookmarkCommentArea;
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return bookmarkCommentArea.getSourceViewer();
	}

	@Override
	public void setBookmark(Bookmark bookmark) {
		bookmarkCommentArea.setBookmark(bookmark);
	}

	@Override
	public boolean canHandle(Bookmark bookmark) {
		// all bookmarks can have comments
		return true;
	}

	@Override
	public void dispose() {
	}

	@Override
	public String getTitle() {
		return "Comments";
	}

}
