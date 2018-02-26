package mesfavoris.internal.views.comment;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.google.common.base.Objects;

import mesfavoris.internal.views.details.AbstractBookmarkDetailPart;
import mesfavoris.model.Bookmark;

/**
 * Create component to display bookmarks comments
 * 
 * @author cchabanois
 *
 */
public class CommentBookmarkDetailPart extends AbstractBookmarkDetailPart {
	private BookmarkCommentArea bookmarkCommentArea;

	@Override
	public void createControl(Composite parent, FormToolkit formToolkit) {
		super.createControl(parent, formToolkit);
		bookmarkCommentArea = new BookmarkCommentArea(parent,
				SWT.MULTI | SWT.V_SCROLL | SWT.WRAP | formToolkit.getBorderStyle(),
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
		super.setBookmark(bookmark);
		bookmarkCommentArea.setBookmark(bookmark);
	}

	@Override
	public boolean canHandle(Bookmark bookmark) {
		// all bookmarks can have comments
		return true;
	}

	@Override
	public String getTitle() {
		return "Comments";
	}

	@Override
	protected void bookmarkModified(Bookmark oldBookmark, Bookmark newBookmark) {
		if (!Objects.equal(bookmarkCommentArea.getBookmark().getPropertyValue(Bookmark.PROPERTY_COMMENT),
				newBookmark.getPropertyValue(Bookmark.PROPERTY_COMMENT))) {
			Display.getDefault().asyncExec(() -> setBookmark(newBookmark));
		}
	}

}
