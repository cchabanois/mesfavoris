package mesfavoris.internal.views.comment;

import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
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

	private ITextListener getTextListener() {
		return new ITextListener() {

			@Override
			public void textChanged(final TextEvent event) {
				if (bookmark == null) {
					return;
				}
				final String newComment = getDocument().get();
				try {
					SetBookmarkCommentOperation operation = new SetBookmarkCommentOperation(bookmarkDatabase);
					operation.setComment(bookmark.getId(), newComment);
				} catch (BookmarksException e) {
					// never happen
				}
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

}
