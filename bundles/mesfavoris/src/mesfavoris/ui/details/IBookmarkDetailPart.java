package mesfavoris.ui.details;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;

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

	void createControl(Composite parent, FormToolkit formToolkit);

	Control getControl();

	ISelectionProvider getSelectionProvider();

	/**
	 * Set the bookmark for this detail part. This method is called if
	 * canHandle(bookmark) returned true
	 * 
	 * @param bookmark
	 *            the bookmark or null if no bookmark is selected
	 */
	void setBookmark(Bookmark bookmark);

	/**
	 * Can this detail part handle this bookmark
	 * 
	 * @param bookmark
	 *            the bookmark or null if no bookmark is selected
	 * @return
	 */
	boolean canHandle(Bookmark bookmark);

	void dispose();
}
