package mesfavoris.internal.views;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;

import mesfavoris.BookmarksException;
import mesfavoris.internal.operations.SetBookmarkCommentOperation;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.validation.IBookmarkModificationValidator;

public class BookmarkCommentViewer extends SourceViewer {
	private static final Cursor SYS_LINK_CURSOR = PlatformUI.getWorkbench().getDisplay()
			.getSystemCursor(SWT.CURSOR_HAND);
	private final BookmarkDatabase bookmarkDatabase;
	private final IBookmarkModificationValidator bookmarkModificationValidator;
	private Bookmark bookmark = null;

	public BookmarkCommentViewer(Composite parent, int styles, BookmarkDatabase bookmarkDatabase,
			IBookmarkModificationValidator bookmarkModificationValidator) {
		super(parent, null, styles);
		this.bookmarkDatabase = bookmarkDatabase;
		this.bookmarkModificationValidator = bookmarkModificationValidator;

		final StyledText t = getTextWidget();

		final Cursor normalCursor = t.getCursor();

		// set the cursor when hovering over a link
		t.addListener(SWT.MouseMove, new Listener() {
			public void handleEvent(final Event e) {
				StyleRange styleRange = getStyleRange(e.x, e.y);
				if (styleRange != null && styleRange.underline)
					t.setCursor(SYS_LINK_CURSOR);
				else
					t.setCursor(normalCursor);
			}
		});

		addTextListener(getTextListener());
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
					SetBookmarkCommentOperation operation = new SetBookmarkCommentOperation(bookmarkDatabase,
							bookmarkModificationValidator);
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
			setDocument(new Document(""));
			setEditable(false);
			return;
		}
		setDocument(new Document(bookmark.getPropertyValue(Bookmark.PROPERTY_COMMENT)));
		setEditable(bookmarkModificationValidator
				.validateModification(bookmarkDatabase.getBookmarksTree(), bookmark.getId()).isOK());
	}

	/**
	 * Get style range at x/y coordinates
	 *
	 * @param x
	 * @param y
	 * @return style range, will be null when no style range exists at given
	 *         coordinates
	 */
	private StyleRange getStyleRange(final int x, final int y) {
		final StyledText t = getTextWidget();
		final int offset;
		try {
			offset = t.getOffsetAtLocation(new Point(x, y));
		} catch (IllegalArgumentException e) {
			return null;
		}
		if (offset < t.getCharCount())
			return t.getStyleRangeAtOffset(offset);
		else
			return null;
	}

}
