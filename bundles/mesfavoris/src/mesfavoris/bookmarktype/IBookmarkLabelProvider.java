package mesfavoris.bookmarktype;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;

import mesfavoris.model.Bookmark;

public interface IBookmarkLabelProvider extends IStyledLabelProvider {

	public boolean handlesBookmark(Bookmark bookmark);
	
}
