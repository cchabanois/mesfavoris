package mesfavoris.internal.adapters;

import java.util.Set;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.views.properties.IPropertySource;

import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.views.properties.BookmarkPropertySource;
import mesfavoris.internal.views.virtual.BookmarkLink;
import mesfavoris.model.Bookmark;
import mesfavoris.problems.BookmarkProblem;

public class BookmarkAdapterFactory implements IAdapterFactory {

	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof Bookmark) {
			Bookmark bookmark = (Bookmark) adaptableObject;
			if (IPropertySource.class.equals(adapterType)) {
				return new BookmarkPropertySource(bookmark, getBookmarkProblems(bookmark));
			}
		}
		if (adaptableObject instanceof BookmarkLink) {
			Bookmark bookmark = ((BookmarkLink)adaptableObject).getBookmark();
			if (IPropertySource.class.equals(adapterType)) {
				return new BookmarkPropertySource(bookmark, getBookmarkProblems(bookmark));
			}
		}
		return null;
	}

	private Set<BookmarkProblem> getBookmarkProblems(Bookmark bookmark) {
		return BookmarksPlugin.getDefault().getBookmarkProblems().getBookmarkProblems(bookmark.getId());
	}
	
	@Override
	public Class[] getAdapterList() {
		return new Class[] { IPropertySource.class };
	}

}
