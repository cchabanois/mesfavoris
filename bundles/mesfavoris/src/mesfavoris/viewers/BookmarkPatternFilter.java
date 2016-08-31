package mesfavoris.viewers;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.dialogs.PatternFilter;

import mesfavoris.model.Bookmark;

/**
 * A filter used in conjunction with <code>FilteredTree</code>.
 * 
 * @see org.eclipse.ui.dialogs.FilteredTree
 */
public class BookmarkPatternFilter extends PatternFilter {

	@Override
	protected boolean isLeafMatch(Viewer viewer, Object element) {
		if (!(element instanceof Bookmark)) {
			return false;
		}
		Bookmark bookmark = (Bookmark) element;
		for (String propValue : bookmark.getProperties().values()) {
			if (wordMatches(propValue)) {
				return true;
			}
		}
		return false;
	}

}
