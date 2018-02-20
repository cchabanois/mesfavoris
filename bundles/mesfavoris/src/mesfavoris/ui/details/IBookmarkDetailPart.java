package mesfavoris.ui.details;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import mesfavoris.internal.views.BookmarksView;
import mesfavoris.model.Bookmark;

/**
 * Interface that must be implemented for a bookmark detail (comment ...)
 * 
 * @author cchabanois
 *
 */
public interface IBookmarkDetailPart {

	String getTitle();

	void initialize(BookmarksView bookmarksView);

	void createControl(Composite parent);

	Control getControl();

	ISelectionProvider getSelectionProvider();

	void setBookmark(Bookmark bookmark);

	boolean canHandle(Bookmark bookmark);

	void dispose();
}
