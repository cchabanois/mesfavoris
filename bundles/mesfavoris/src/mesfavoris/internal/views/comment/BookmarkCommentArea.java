package mesfavoris.internal.views.comment;

import org.eclipse.jface.text.ITextListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Composite;

import mesfavoris.BookmarksException;
import mesfavoris.internal.service.operations.SetBookmarkCommentOperation;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;

public class BookmarkCommentArea extends SpellcheckableMessageArea {
	private final BookmarkDatabase bookmarkDatabase;
	private Bookmark bookmark = null;

	public BookmarkCommentArea(Composite parent, int styles, BookmarkDatabase bookmarkDatabase) {
		super(parent, null, styles);
		this.bookmarkDatabase = bookmarkDatabase;

		getSourceViewer().addTextListener(getTextListener());
	}

	@Override
	public void addFocusListener(FocusListener listener) {
		getSourceViewer().getControl().addFocusListener(listener);
	}
	
	@Override
	public void removeFocusListener(FocusListener listener) {
		getSourceViewer().getControl().removeFocusListener(listener);
	}
	
	private ITextListener getTextListener() {
		return event->{
			if (bookmark == null || !getSourceViewer().isEditable()) {
				return;
			}
			final String newComment = getDocument().get();
			try {
				SetBookmarkCommentOperation operation = new SetBookmarkCommentOperation(bookmarkDatabase);
				operation.setComment(bookmark.getId(), newComment);
				bookmark = bookmarkDatabase.getBookmarksTree().getBookmark(bookmark.getId());
			} catch (BookmarksException e) {
				// never happen
			}			
		};
	}

	public void setBookmark(final Bookmark bookmark) {
		this.bookmark = bookmark;
		if (bookmark == null) {
			setText("");
			getSourceViewer().setEditable(false);
			return;
		}
		String comment = bookmark.getPropertyValue(Bookmark.PROPERTY_COMMENT);
		if (comment == null) {
			comment = "";
		}
		setText(comment);
		getSourceViewer().setEditable(bookmarkDatabase.getBookmarksModificationValidator()
				.validateModification(bookmarkDatabase.getBookmarksTree(), bookmark.getId()).isOK());
	}
	
	public Bookmark getBookmark() {
		return bookmark;
	}

}
