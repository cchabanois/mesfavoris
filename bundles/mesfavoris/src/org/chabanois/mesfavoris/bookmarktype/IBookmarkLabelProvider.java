package org.chabanois.mesfavoris.bookmarktype;

import org.chabanois.mesfavoris.model.Bookmark;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;

public interface IBookmarkLabelProvider extends IStyledLabelProvider {

	public boolean handlesBookmark(Bookmark bookmark);
	
}
